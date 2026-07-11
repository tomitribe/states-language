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

import io.github.aglibs.validcheck.ValidCheck;
import jakarta.json.JsonValue;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import lombok.Builder;
import lombok.Singular;

import java.util.List;

/**
 * Processes all elements of an array, potentially in parallel, running the
 * {@code itemProcessor} once per item.  Where the Parallel State applies
 * different branches to the same input, the Map State applies a single
 * state machine to multiple input elements.
 *
 * If no {@code itemReader} or {@code items} is provided, the items array is
 * the state input, which must be a JSON array.  If no {@code resultWriter}
 * is provided, the result is a JSON array with one element per item, in the
 * same order as the items array regardless of completion order.
 *
 * @param comment human-readable description of the state
 * @param queryLanguage overrides the state machine's query language; this model targets JSONata
 * @param items the items array; a JSON array or a JSONata string producing one
 * @param itemReader reads items from an external resource instead of the state input
 * @param itemSelector overrides each single element of the items array; a JSON value or a JSONata string
 * @param itemBatcher batches selected items into sub-arrays before invocation
 * @param resultWriter writes results to an external resource instead of the state result
 * @param itemProcessor the state machine executed once per item or batch; required
 * @param maxConcurrency upper bound on concurrent iterations; zero (the default) means no limit
 * @param toleratedFailurePercentage percentage of failed items tolerated before the state fails, 0 to 100
 * @param toleratedFailureCount number of failed items tolerated before the state fails
 * @param output the state output; a JSON value or a JSONata string, may reference $states.result
 * @param assign variable assignments; values may be JSONata strings, effective in the next state
 * @param retry retry policies scanned in order when the state reports an error
 * @param catchers fallback transitions scanned in order when retries are exhausted
 * @param next name of the state to transition to; exactly one of next or end is required
 * @param end true marks this state as terminal
 * @see <a href="https://states-language.net/spec.html#map-state">Map State</a>
 */
@Builder(toBuilder = true, builderClassName = "Builder")
public record MapState(@JsonbProperty("Comment") String comment,
                       @JsonbProperty("QueryLanguage") String queryLanguage,
                       @JsonbProperty("Items") JsonValue items,
                       @JsonbProperty("ItemReader") ItemReader itemReader,
                       @JsonbProperty("ItemSelector") JsonValue itemSelector,
                       @JsonbProperty("ItemBatcher") ItemBatcher itemBatcher,
                       @JsonbProperty("ResultWriter") ResultWriter resultWriter,
                       @JsonbProperty("ItemProcessor") ItemProcessor itemProcessor,
                       @JsonbProperty("MaxConcurrency") Integer maxConcurrency,
                       @JsonbProperty("ToleratedFailurePercentage") Double toleratedFailurePercentage,
                       @JsonbProperty("ToleratedFailureCount") Integer toleratedFailureCount,
                       @JsonbProperty("Output") @JsonbTypeAdapter(Output.Adapter.class) Output output,
                       @JsonbProperty("Assign") @JsonbTypeAdapter(Assign.Adapter.class) Assign assign,
                       @JsonbProperty("Retry") @Singular("retrier") List<Retrier> retry,
                       @JsonbProperty("Catch") @Singular("catcher") List<Catcher> catchers,
                       @JsonbProperty("Next") String next,
                       @JsonbProperty("End") Boolean end) implements State {
    public MapState {
        ValidCheck.requireNotNull(itemProcessor, "itemProcessor");
        retry = retry == null || retry.isEmpty() ? null : List.copyOf(retry);
        catchers = catchers == null || catchers.isEmpty() ? null : List.copyOf(catchers);
    }
}
