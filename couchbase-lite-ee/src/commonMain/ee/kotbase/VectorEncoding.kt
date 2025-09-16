/*
 * Copyright 2024 Jeff Lockhart
 *
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
package kotbase

public expect class VectorEncoding {

    /**
     * Scalar Quantizer Encoding Type
     */
    public enum class ScalarQuantizerType {

        /**
         * 4 bits per dimension
         */
        SQ4,

        /**
         * 6 bits per dimension
         */
        SQ6,

        /**
         * 8 bits per dimension
         */
        SQ8
    }

    public companion object {

        /**
         * No encoding; 4 bytes per dimension, no data loss
         */
        public fun none(): VectorEncoding

        /**
         * Scalar Quantizer encoding
         */
        public fun scalarQuantizer(type: ScalarQuantizerType): VectorEncoding

        /**
         * Product Quantizer encoding.
         *
         * @param subquantizers Number of subquantizers (divisor of the # of dimension.)
         * @param bits          Number of bits: between 4 and 12 inclusive.
         */
        public fun productQuantizer(subquantizers: Long, bits: Long): VectorEncoding
    }
}
