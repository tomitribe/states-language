/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.aws.states;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * The whole-document States Language rules, reachable through
 * {@link StateMachine#validate()}: transition targets resolve within their
 * own scope, state names are unique across the entire machine, variables
 * do not shadow outer scopes, the effective query language is JSONata, and
 * the runtime-risk warnings.  Rules decidable from a single object's own
 * fields are enforced by the record constructors instead; see Rules.
 *
 * Deliberately not public: callers validate by calling
 * {@code machine.validate()}.
 */
final class Validations {

    private Validations() {
    }

    static Validation validate(final StateMachine machine) {
        final List<Finding> findings = new ArrayList<>();

        if (!"JSONata".equals(machine.queryLanguage())) {
            findings.add(Finding.error("QueryLanguage", machine.queryLanguage() == null
                    ? "\"QueryLanguage\" is absent, so the machine defaults to JSONPath; this model"
                        + " writes JSONata fields.  Set queryLanguage(\"JSONata\")"
                    : String.format("\"QueryLanguage\" is \"%s\"; this model writes JSONata fields."
                        + "  Set queryLanguage(\"JSONata\")", machine.queryLanguage())));
        }

        final Map<String, List<String>> index = new LinkedHashMap<>();
        index(machine.states(), "States", index);
        index.forEach((name, paths) -> {
            if (paths.size() > 1) {
                findings.add(Finding.error(paths.get(1), String.format(
                        "State name \"%s\" is defined %s times: %s."
                                + "  State names must be unique within the entire state machine",
                        name, paths.size(), String.join(", ", paths))));
            }
        });

        scope(machine.startAt(), machine.states(), "States", "StartAt", Map.of(), index, findings);

        return new Validation(findings);
    }

    private static void index(final Map<String, State> states, final String path,
                              final Map<String, List<String>> index) {
        states.forEach((name, state) -> {
            final String statePath = path + "." + name;
            index.computeIfAbsent(name, key -> new ArrayList<>()).add(statePath);

            if (state instanceof ParallelState parallel) {
                for (int i = 0; i < parallel.branches().size(); i++) {
                    index(parallel.branches().get(i).states(),
                            statePath + ".Branches[" + i + "].States", index);
                }
            }
            if (state instanceof MapState map) {
                index(map.itemProcessor().states(), statePath + ".ItemProcessor.States", index);
            }
        });
    }

    private static void scope(final String startAt, final Map<String, State> states,
                              final String path, final String startAtPath,
                              final Map<String, String> outerVariables,
                              final Map<String, List<String>> index,
                              final List<Finding> findings) {
        if (!states.containsKey(startAt)) {
            findings.add(Finding.error(startAtPath, String.format(
                    "\"StartAt\": \"%s\" does not match any state in %s.  States present: %s",
                    startAt, path, String.join(", ", states.keySet()))));
        }

        final Map<String, String> variables = new LinkedHashMap<>();

        states.forEach((name, state) -> {
            final String statePath = path + "." + name;

            if (state.queryLanguage() != null && !"JSONata".equals(state.queryLanguage())) {
                findings.add(Finding.error(statePath, String.format(
                        "\"QueryLanguage\": \"%s\" overrides the machine's query language; this"
                                + " model writes JSONata fields", state.queryLanguage())));
            }

            targets(state).forEach((field, target) ->
                    checkTarget(field, target, statePath, states, index, findings));

            for (final Assign assign : assigns(state)) {
                for (final String variable : assign.names()) {
                    final String outer = outerVariables.get(variable);
                    if (outer != null) {
                        findings.add(Finding.error(statePath, String.format(
                                "Variable \"%s\" is already assigned in the outer scope at %s."
                                        + "  Outer and inner variable names must be unique",
                                variable, outer)));
                    }
                    variables.putIfAbsent(variable, statePath);
                }
            }

            if (state instanceof TaskState task) {
                duplicateErrors("Retry", errorLists(task.retry(), Retrier::errorEquals), statePath, findings);
                duplicateErrors("Catch", errorLists(task.catchers(), Catcher::errorEquals), statePath, findings);
            } else if (state instanceof ParallelState parallel) {
                duplicateErrors("Retry", errorLists(parallel.retry(), Retrier::errorEquals), statePath, findings);
                duplicateErrors("Catch", errorLists(parallel.catchers(), Catcher::errorEquals), statePath, findings);
            } else if (state instanceof MapState map) {
                duplicateErrors("Retry", errorLists(map.retry(), Retrier::errorEquals), statePath, findings);
                duplicateErrors("Catch", errorLists(map.catchers(), Catcher::errorEquals), statePath, findings);
            }

            if (state instanceof ChoiceState choice && choice.defaultState() == null) {
                findings.add(Finding.warning(statePath,
                        "No \"Default\"; if no Choice Rule matches, the machine fails with"
                                + " States.NoChoiceMatched.  Add a \"Default\" or a final rule"
                                + " with a true Condition"));
            }
            if (state instanceof MapState map && map.items() == null && map.itemReader() == null) {
                findings.add(Finding.warning(statePath,
                        "Neither \"Items\" nor \"ItemReader\"; at runtime the state input"
                                + " must be a JSON array"));
            }
        });

        unreachable(startAt, states).forEach(name -> findings.add(Finding.warning(
                path + "." + name,
                "Unreachable; no transition targets it and it is not the \"StartAt\"")));

        final Map<String, String> combined = new LinkedHashMap<>(outerVariables);
        combined.putAll(variables);

        states.forEach((name, state) -> {
            final String statePath = path + "." + name;
            if (state instanceof ParallelState parallel) {
                for (int i = 0; i < parallel.branches().size(); i++) {
                    final Branch branch = parallel.branches().get(i);
                    final String branchPath = statePath + ".Branches[" + i + "]";
                    scope(branch.startAt(), branch.states(), branchPath + ".States",
                            branchPath + ".StartAt", combined, index, findings);
                }
            }
            if (state instanceof MapState map) {
                final ItemProcessor processor = map.itemProcessor();
                scope(processor.startAt(), processor.states(), statePath + ".ItemProcessor.States",
                        statePath + ".ItemProcessor.StartAt", combined, index, findings);
            }
        });
    }

    private static void checkTarget(final String field, final String target, final String statePath,
                                    final Map<String, State> states,
                                    final Map<String, List<String>> index,
                                    final List<Finding> findings) {
        if (states.containsKey(target)) return;

        final List<String> elsewhere = index.get(target);
        if (elsewhere != null) {
            findings.add(Finding.error(statePath, String.format(
                    "\"%s\": \"%s\" targets a state outside its own \"States\" field (defined at %s)."
                            + "  States may only transition within their own scope",
                    field, target, String.join(", ", elsewhere))));
        } else {
            findings.add(Finding.error(statePath, String.format(
                    "\"%s\": \"%s\" does not match any state.  States present in this scope: %s",
                    field, target, String.join(", ", states.keySet()))));
        }
    }

    /**
     * The named transitions out of a state, field description to target
     */
    private static Map<String, String> targets(final State state) {
        final Map<String, String> targets = new LinkedHashMap<>();

        if (state instanceof TaskState task) {
            put(targets, "Next", task.next());
            catchers(targets, task.catchers());
        } else if (state instanceof ParallelState parallel) {
            put(targets, "Next", parallel.next());
            catchers(targets, parallel.catchers());
        } else if (state instanceof MapState map) {
            put(targets, "Next", map.next());
            catchers(targets, map.catchers());
        } else if (state instanceof PassState pass) {
            put(targets, "Next", pass.next());
        } else if (state instanceof WaitState wait) {
            put(targets, "Next", wait.next());
        } else if (state instanceof ChoiceState choice) {
            for (int i = 0; i < choice.choices().size(); i++) {
                put(targets, "Choices[" + i + "].Next", choice.choices().get(i).next());
            }
            put(targets, "Default", choice.defaultState());
        }

        return targets;
    }

    private static void catchers(final Map<String, String> targets, final List<Catcher> catchers) {
        if (catchers == null) return;
        for (int i = 0; i < catchers.size(); i++) {
            put(targets, "Catch[" + i + "].Next", catchers.get(i).next());
        }
    }

    private static void put(final Map<String, String> targets, final String field, final String target) {
        if (target != null) targets.put(field, target);
    }

    private static List<Assign> assigns(final State state) {
        final List<Assign> assigns = new ArrayList<>();

        if (state instanceof TaskState task) {
            add(assigns, task.assign());
            if (task.catchers() != null) task.catchers().forEach(catcher -> add(assigns, catcher.assign()));
        } else if (state instanceof ParallelState parallel) {
            add(assigns, parallel.assign());
            if (parallel.catchers() != null) parallel.catchers().forEach(catcher -> add(assigns, catcher.assign()));
        } else if (state instanceof MapState map) {
            add(assigns, map.assign());
            if (map.catchers() != null) map.catchers().forEach(catcher -> add(assigns, catcher.assign()));
        } else if (state instanceof PassState pass) {
            add(assigns, pass.assign());
        } else if (state instanceof WaitState wait) {
            add(assigns, wait.assign());
        } else if (state instanceof ChoiceState choice) {
            add(assigns, choice.assign());
            choice.choices().forEach(rule -> add(assigns, rule.assign()));
        }

        return assigns;
    }

    private static void add(final List<Assign> assigns, final Assign assign) {
        if (assign != null) assigns.add(assign);
    }

    private static <T> List<List<String>> errorLists(final List<T> entries,
                                                     final Function<T, List<String>> errors) {
        if (entries == null) return List.of();
        return entries.stream().map(errors).toList();
    }

    /**
     * The interpreter uses the first Retrier or Catcher matching an error
     * name, and an exhausted Retrier falls through to Catch rather than to
     * later Retriers — so a repeated name is legal but dead
     */
    private static void duplicateErrors(final String field, final List<List<String>> entries,
                                        final String statePath, final List<Finding> findings) {
        final Map<String, Integer> seen = new LinkedHashMap<>();
        for (int i = 0; i < entries.size(); i++) {
            for (final String error : entries.get(i)) {
                final Integer first = seen.putIfAbsent(error, i);
                if (first == null) continue;
                findings.add(Finding.warning(statePath, first == i
                        ? String.format("\"%s\" is listed twice in %s[%s].\"ErrorEquals\"",
                                error, field, i)
                        : String.format("\"%s\" in %s[%s] is unreachable: %s[%s] already matches it,"
                                        + " and the interpreter uses the first match",
                                error, field, i, field, first)));
            }
        }
    }

    private static Set<String> unreachable(final String startAt, final Map<String, State> states) {
        final Set<String> visited = new LinkedHashSet<>();
        final Deque<String> queue = new ArrayDeque<>();
        if (states.containsKey(startAt)) queue.add(startAt);

        while (!queue.isEmpty()) {
            final String name = queue.remove();
            if (!visited.add(name)) continue;
            targets(states.get(name)).values().stream()
                    .filter(states::containsKey)
                    .forEach(queue::add);
        }

        final Set<String> unreachable = new LinkedHashSet<>(states.keySet());
        unreachable.removeAll(visited);
        return unreachable;
    }
}
