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

public actual class VectorEncoding
private constructor(internal val actual: CPointer<CBLVectorEncoding>) {

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        CBLVectorEncoding_Free(it)
    }

    public actual enum class ScalarQuantizerType {
        SQ4,
        SQ6,
        SQ8;

        internal val actual: CBLScalarQuantizerType
            get() = when (this) {
                SQ4 -> kCBLSQ4
                SQ6 -> kCBLSQ6
                SQ8 -> kCBLSQ8
            }
    }

    public actual companion object {

        public actual fun none(): VectorEncoding =
            VectorEncoding(CBLVectorEncoding_CreateNone()!!)

        public actual fun scalarQuantizer(type: ScalarQuantizerType): VectorEncoding =
            VectorEncoding(CBLVectorEncoding_CreateScalarQuantizer(type.actual)!!)

        public actual fun productQuantizer(subquantizers: Long, bits: Long): VectorEncoding =
            VectorEncoding(CBLVectorEncoding_CreateProductQuantizer(subquantizers.convert(), bits.convert())!!)
    }
}
