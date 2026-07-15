/*
 * Tomitribe Confidential
 *
 * Copyright Tomitribe Corporation. 2026
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */
package com.tomitribe.aureto.states;

import com.tomitribe.aureto.states.test.JsonAsserts;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationTest {

    /**
     * The machines the rest of the suite round-trips are themselves valid
     */
    @Test
    public void cleanMachinesValidate() throws Exception {
        for (final String name : new String[]{"DistributeRelease", "ControlFlow", "DistributedMap"}) {
            final String json = JsonAsserts.resource("StateMachineJsonTest/" + name + ".json");
            try (Jsonb jsonb = JsonbBuilder.create()) {
                final Validation validation = jsonb.fromJson(json, StateMachine.class).validate();
                assertTrue(validation.findings().isEmpty(), name + ": " + validation.findings());
                assertSame(validation, validation.requireValid());
            }
        }
    }

    @Test
    public void startAtMustResolve() {
        final Validation validation = machine("Nope", Map.of("A", pass())).validate();

        assertEquals(1, validation.errors().size());
        final Finding finding = validation.errors().get(0);
        assertEquals("StartAt", finding.path());
        assertEquals("\"StartAt\": \"Nope\" does not match any state in States."
                + "  States present: A", finding.message());
    }

    @Test
    public void transitionTargetsMustResolve() {
        final Validation validation = machine("A", Map.of("A", pass("B"))).validate();

        assertEquals(1, validation.errors().size());
        final Finding finding = validation.errors().get(0);
        assertEquals("States.A", finding.path());
        assertEquals("\"Next\": \"B\" does not match any state."
                + "  States present in this scope: A", finding.message());
    }

    @Test
    public void crossScopeTargetsNameTheScopeRule() {
        final MapState hop = MapState.builder()
                .items(Items.expression("$states.input"))
                .itemProcessor(ItemProcessor.builder()
                        .startAt("Hop")
                        .states(Map.of("Hop", pass("Finish")))
                        .build())
                .next("Finish")
                .build();

        final Validation validation = machine("Begin", Map.of(
                "Begin", hop,
                "Finish", pass())).validate();

        assertEquals(1, validation.errors().size());
        final Finding finding = validation.errors().get(0);
        assertEquals("States.Begin.ItemProcessor.States.Hop", finding.path());
        assertEquals("\"Next\": \"Finish\" targets a state outside its own \"States\" field"
                + " (defined at States.Finish)."
                + "  States may only transition within their own scope", finding.message());
    }

    @Test
    public void stateNamesAreUniqueAcrossTheEntireMachine() {
        final MapState work = MapState.builder()
                .items(Items.expression("$states.input"))
                .itemProcessor(ItemProcessor.builder()
                        .startAt("Work")
                        .states(Map.of("Work", pass()))
                        .build())
                .end(true)
                .build();

        final Validation validation = machine("Work", Map.of("Work", work)).validate();

        assertEquals(1, validation.errors().size());
        assertEquals("State name \"Work\" is defined 2 times:"
                + " States.Work, States.Work.ItemProcessor.States.Work."
                + "  State names must be unique within the entire state machine",
                validation.errors().get(0).message());
    }

    @Test
    public void queryLanguageMustBeJsonata() {
        final Validation absent = StateMachine.builder()
                .startAt("A")
                .states(Map.of("A", pass()))
                .build()
                .validate();

        assertEquals(1, absent.errors().size());
        assertEquals("QueryLanguage", absent.errors().get(0).path());
        assertEquals("\"QueryLanguage\" is absent, so the machine defaults to JSONPath;"
                + " this model writes JSONata fields.  Set queryLanguage(\"JSONata\")",
                absent.errors().get(0).message());

        final Validation overridden = machine("A", Map.of("A", PassState.builder()
                .queryLanguage("JSONPath")
                .end(true)
                .build())).validate();

        assertEquals(1, overridden.errors().size());
        assertEquals("\"QueryLanguage\": \"JSONPath\" overrides the machine's query language;"
                + " this model writes JSONata fields", overridden.errors().get(0).message());
    }

    @Test
    public void variablesMustNotShadowOuterScopes() {
        final MapState loop = MapState.builder()
                .items(Items.expression("$states.input"))
                .itemProcessor(ItemProcessor.builder()
                        .startAt("Inner")
                        .states(Map.of("Inner", PassState.builder()
                                .assign(Assign.builder().value("x", 2).build())
                                .end(true)
                                .build()))
                        .build())
                .end(true)
                .build();

        final Validation validation = machine("Setup", Map.of(
                "Setup", PassState.builder()
                        .assign(Assign.builder().value("x", 1).build())
                        .next("Loop")
                        .build(),
                "Loop", loop)).validate();

        assertEquals(1, validation.errors().size());
        final Finding finding = validation.errors().get(0);
        assertEquals("States.Loop.ItemProcessor.States.Inner", finding.path());
        assertEquals("Variable \"x\" is already assigned in the outer scope at States.Setup."
                + "  Outer and inner variable names must be unique", finding.message());
    }

    @Test
    public void unreachableStatesWarn() {
        final Validation validation = machine("A", Map.of(
                "A", pass(),
                "B", pass())).validate();

        assertTrue(validation.isValid());
        assertEquals(1, validation.warnings().size());
        assertEquals("States.B", validation.warnings().get(0).path());
        assertEquals("Unreachable; no transition targets it and it is not the \"StartAt\"",
                validation.warnings().get(0).message());
    }

    @Test
    public void choiceWithoutDefaultWarns() {
        final Validation validation = machine("Decide", Map.of(
                "Decide", ChoiceState.builder()
                        .choice(ChoiceRule.builder()
                                .condition(Condition.of(true))
                                .next("Done")
                                .build())
                        .build(),
                "Done", SucceedState.builder().build())).validate();

        assertTrue(validation.isValid());
        assertEquals(1, validation.warnings().size());
        assertEquals("No \"Default\"; if no Choice Rule matches, the machine fails with"
                + " States.NoChoiceMatched.  Add a \"Default\" or a final rule"
                + " with a true Condition", validation.warnings().get(0).message());
    }

    @Test
    public void mapWithoutItemsWarns() {
        final Validation validation = machine("Work", Map.of("Work", MapState.builder()
                .itemProcessor(ItemProcessor.builder()
                        .startAt("Inner")
                        .states(Map.of("Inner", pass()))
                        .build())
                .end(true)
                .build())).validate();

        assertTrue(validation.isValid());
        assertEquals(1, validation.warnings().size());
        assertEquals("Neither \"Items\" nor \"ItemReader\"; at runtime the state input"
                + " must be a JSON array", validation.warnings().get(0).message());
    }

    @Test
    public void requireValidReportsEveryError() {
        final StateMachine machine = machine("Nope", Map.of("A", pass("B")));

        final IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> machine.validate().requireValid());

        assertTrue(failure.getMessage().startsWith("The state machine is not valid:\n"),
                failure.getMessage());
        assertTrue(failure.getMessage().contains("StartAt: \"StartAt\": \"Nope\""), failure.getMessage());
        assertTrue(failure.getMessage().contains("States.A: \"Next\": \"B\""), failure.getMessage());
    }

    private StateMachine machine(final String startAt, final Map<String, State> states) {
        return StateMachine.builder()
                .queryLanguage("JSONata")
                .startAt(startAt)
                .states(states)
                .build();
    }

    private PassState pass() {
        return PassState.builder().end(true).build();
    }

    private PassState pass(final String next) {
        return PassState.builder().next(next).build();
    }
}
