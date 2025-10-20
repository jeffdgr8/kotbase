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

import cnames.structs.CBLBlob
import cnames.structs.CBLCollection
import cnames.structs.CBLDatabase
import cnames.structs.CBLDocument
import cnames.structs.CBLQuery
import cnames.structs.CBLQueryIndex
import cnames.structs.CBLReplicator
import cnames.structs.CBLScope
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents
import libcblite.CBLBlob_Retain
import libcblite.CBLCollection_Retain
import libcblite.CBLDatabase_Retain
import libcblite.CBLDocument_Retain
import libcblite.CBLQueryIndex_Retain
import libcblite.CBLQuery_Retain
import libcblite.CBLReplicator_Retain
import libcblite.CBLScope_Retain
import libcblite.FLArray
import libcblite.FLArray_Retain
import libcblite.FLDict
import libcblite.FLDict_Retain
import libcblite.FLDoc
import libcblite.FLDoc_Retain
import libcblite.FLMutableArray
import libcblite.FLMutableArray_Retain
import libcblite.FLMutableDict
import libcblite.FLMutableDict_Retain
import libcblite.FLSliceResult
import libcblite.FLSliceResult_Retain
import libcblite.FLValue
import libcblite.FLValue_Retain

// Regex to find retain calls not from this package:
// (?<!\.)\b\w+_Retain\b

internal fun CBLBlob_Retain(t: CPointer<CBLBlob>?): CPointer<CBLBlob>? {
    RefTracker.trackRetain(t, "CBLBlob")
    return CBLBlob_Retain(t)
}

internal fun CBLCollection_Retain(t: CPointer<CBLCollection>?): CPointer<CBLCollection>? {
    RefTracker.trackRetain(t, "CBLCollection")
    return CBLCollection_Retain(t)
}

internal fun CBLDatabase_Retain(t: CPointer<CBLDatabase>?): CPointer<CBLDatabase>? {
    RefTracker.trackRetain(t, "CBLDatabase")
    return CBLDatabase_Retain(t)
}

internal fun CBLDocument_Retain(t: CPointer<CBLDocument>?): CPointer<CBLDocument>? {
    RefTracker.trackRetain(t, "CBLDocument")
    return CBLDocument_Retain(t)
}

internal fun FLMutableArray_Retain(d: FLMutableArray?): FLMutableArray? {
    RefTracker.trackRetain(d, "FLArray")
    return FLMutableArray_Retain(d)
}

internal fun CBLQuery_Retain(t: CPointer<CBLQuery>?): CPointer<CBLQuery>? {
    RefTracker.trackRetain(t, "CBLQuery")
    return CBLQuery_Retain(t)
}

internal fun CBLQueryIndex_Retain(t: CPointer<CBLQueryIndex>?): CPointer<CBLQueryIndex>? {
    RefTracker.trackRetain(t, "CBLQueryIndex")
    return CBLQueryIndex_Retain(t)
}

internal fun CBLReplicator_Retain(t: CPointer<CBLReplicator>?): CPointer<CBLReplicator>? {
    RefTracker.trackRetain(t, "CBLReplicator")
    return CBLReplicator_Retain(t)
}

internal fun CBLScope_Retain(t: CPointer<CBLScope>?): CPointer<CBLScope>? {
    RefTracker.trackRetain(t, "CBLScope")
    return CBLScope_Retain(t)
}

internal fun FLArray_Retain(v: FLArray?): FLArray? {
    RefTracker.trackRetain(v, "FLArray")
    return FLArray_Retain(v)
}

internal fun FLDict_Retain(v: FLDict?): FLDict? {
    RefTracker.trackRetain(v, "FLDict")
    return FLDict_Retain(v)
}

internal fun FLDoc_Retain(arg0: FLDoc?): FLDoc? {
    RefTracker.trackRetain(arg0, "FLDoc")
    return FLDoc_Retain(arg0)
}

internal fun FLSliceResult_Retain(s: CValue<FLSliceResult>): CValue<FLSliceResult> {
    RefTracker.trackRetain(s.useContents { buf }, "FLSliceResult")
    return FLSliceResult_Retain(s)
}

internal fun FLMutableDict_Retain(d: FLMutableDict?): FLMutableDict? {
    RefTracker.trackRetain(d, "FLDict")
    return FLMutableDict_Retain(d)
}

internal fun FLValue_Retain(arg0: FLValue?): FLValue? {
    RefTracker.trackRetain(arg0, "FLValue")
    return FLValue_Retain(arg0)
}
