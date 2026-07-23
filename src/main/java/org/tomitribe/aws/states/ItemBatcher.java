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
    public ItemBatcher {
        if (maxItemsPerBatch == null && maxInputBytesPerBatch == null) {
            throw new IllegalArgumentException(
                    "ItemBatcher must have at least one of \"MaxItemsPerBatch\" or \"MaxInputBytesPerBatch\"");
        }
        Rules.requirePositive("MaxItemsPerBatch", maxItemsPerBatch);
        Rules.requirePositive("MaxInputBytesPerBatch", maxInputBytesPerBatch);
    }
}
