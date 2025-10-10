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

import cnames.structs.CBLVectorEncoding
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.convert
import libcblite.*
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual open class VectorEncoding
private constructor(internal val actual: CPointer<CBLVectorEncoding>) {

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        CBLVectorEncoding_Free(it)
    }

    public actual enum class ScalarQuantizerType(internal val bits: Int) {
        SQ4(4),
        SQ6(6),
        SQ8(8);

        internal val actual: CBLScalarQuantizerType
            get() = when (this) {
                SQ4 -> kCBLSQ4
                SQ6 -> kCBLSQ6
                SQ8 -> kCBLSQ8
            }
    }

    private class NoneVectorEncoding() : VectorEncoding(CBLVectorEncoding_CreateNone()!!) {

        override fun equals(other: Any?): Boolean =
            this === other || other is NoneVectorEncoding

        override fun hashCode(): Int = 1
    }

    private class ScalarVectorEncoding(
        private val type: ScalarQuantizerType
    ) : VectorEncoding(CBLVectorEncoding_CreateScalarQuantizer(type.actual)!!) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ScalarVectorEncoding) return false
            return type.bits == other.type.bits
        }

        override fun hashCode(): Int =
            type.bits
    }

    private class ProductVectorEncoding(
        private val subquantizers: Long,
        private val bits: Long
    ) : VectorEncoding(CBLVectorEncoding_CreateProductQuantizer(subquantizers.convert(), bits.convert())!!) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ProductVectorEncoding) return false
            return (bits == other.bits) && (subquantizers == other.subquantizers)
        }

        override fun hashCode(): Int =
            ((bits * 37) + subquantizers).toInt()
    }

    public actual companion object {

        public actual fun none(): VectorEncoding =
            NoneVectorEncoding()

        public actual fun scalarQuantizer(type: ScalarQuantizerType): VectorEncoding =
            ScalarVectorEncoding(type)

        public actual fun productQuantizer(subquantizers: Long, bits: Long): VectorEncoding =
            ProductVectorEncoding(subquantizers, bits)
    }
}
