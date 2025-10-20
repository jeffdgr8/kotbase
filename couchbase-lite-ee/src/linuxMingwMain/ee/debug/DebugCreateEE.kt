/*
 * Copyright 2025 Jeff Lockhart
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
package debug

import cnames.structs.CBLDatabase
import cnames.structs.CBLEndpoint
import cnames.structs.CBLVectorEncoding
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef
import libcblite.CBLEndpoint_CreateWithLocalDB
import libcblite.CBLScalarQuantizerType
import libcblite.CBLVectorEncoding_CreateNone
import libcblite.CBLVectorEncoding_CreateProductQuantizer
import libcblite.CBLVectorEncoding_CreateScalarQuantizer

// Regex to find create calls not from this package:
// (?<!\.)\b\w+_Create
// (?<!\.)\b\w+_New
// (?<!\.)\b\w+_MutableCopy\b

internal fun CBLEndpoint_CreateWithLocalDB(arg0: CValuesRef<CBLDatabase>?): CPointer<CBLEndpoint>? {
    return CBLEndpoint_CreateWithLocalDB(arg0).also {
        RefTracker.trackRetain(it, "CBLEndpoint")
    }
}

internal fun CBLVectorEncoding_CreateProductQuantizer(subquantizers: UInt, bits: UInt): CPointer<CBLVectorEncoding>? {
    return CBLVectorEncoding_CreateProductQuantizer(subquantizers, bits).also {
        RefTracker.trackRetain(it, "CBLVectorEncoding")
    }
}

internal fun CBLVectorEncoding_CreateScalarQuantizer(type: CBLScalarQuantizerType): CPointer<CBLVectorEncoding>? {
    return CBLVectorEncoding_CreateScalarQuantizer(type).also {
        RefTracker.trackRetain(it, "CBLVectorEncoding")
    }
}

internal fun CBLVectorEncoding_CreateNone(): CPointer<CBLVectorEncoding>? {
    return CBLVectorEncoding_CreateNone().also {
        RefTracker.trackRetain(it, "CBLVectorEncoding")
    }
}
