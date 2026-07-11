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

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import lombok.Builder;

/**
 * Causes a Map State to batch selected items into sub-arrays before passing
 * them to each ItemProcessor invocation.  At least one of
 * {@code maxItemsPerBatch} or {@code maxInputBytesPerBatch} must be
 * provided.
 *
 * @param batchInput fixed input merged into each batch; a JSON value or a JSONata string
 * @param maxItemsPerBatch maximum number of items per sub-array
 * @param maxInputBytesPerBatch maximum size in bytes of each sub-array
 * @see <a href="https://states-language.net/spec.html#batching-items">Batching Items</a>
 */
@Builder(toBuilder = true, builderClassName = "Builder")
public record ItemBatcher(@JsonbProperty("BatchInput") @JsonbTypeAdapter(BatchInput.Adapter.class) BatchInput batchInput,
                          @JsonbProperty("MaxItemsPerBatch") Integer maxItemsPerBatch,
                          @JsonbProperty("MaxInputBytesPerBatch") Integer maxInputBytesPerBatch) {
}
