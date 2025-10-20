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

import cnames.structs.CBLAuthenticator
import cnames.structs.CBLBlob
import cnames.structs.CBLCollection
import cnames.structs.CBLDatabase
import cnames.structs.CBLDocument
import cnames.structs.CBLEndpoint
import cnames.structs.CBLQuery
import cnames.structs.CBLQueryIndex
import cnames.structs.CBLReplicator
import cnames.structs.CBLResultSet
import cnames.structs.CBLScope
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents
import libcblite.CBLAuth_Free
import libcblite.CBLBlob_Release
import libcblite.CBLCollection_Release
import libcblite.CBLDatabase_Release
import libcblite.CBLDocument_Release
import libcblite.CBLEndpoint_Free
import libcblite.CBLQueryIndex_Release
import libcblite.CBLQuery_Release
import libcblite.CBLReplicator_Release
import libcblite.CBLResultSet_Release
import libcblite.CBLScope_Release
import libcblite.FLArray
import libcblite.FLArray_Release
import libcblite.FLDict
import libcblite.FLDict_Release
import libcblite.FLDoc
import libcblite.FLDoc_Release
import libcblite.FLMutableArray
import libcblite.FLMutableArray_Release
import libcblite.FLMutableDict
import libcblite.FLMutableDict_Release
import libcblite.FLSliceResult
import libcblite.FLSliceResult_Release
import libcblite.FLValue
import libcblite.FLValue_Release

// Regex to find release calls not from this package:
// (?<!\.)\b\w+_Release\b
// (?<!\.)\b\w+_Free\b

internal fun CBLAuth_Free(arg0: CPointer<CBLAuthenticator>?) {
    RefTracker.trackRelease(arg0, "CBLAuthenticator")
    CBLAuth_Free(arg0)
}

internal fun CBLBlob_Release(t: CPointer<CBLBlob>?) {
    RefTracker.trackRelease(t, "CBLBlob")
    CBLBlob_Release(t)
}

internal fun CBLCollection_Release(t: CPointer<CBLCollection>?) {
    RefTracker.trackRelease(t, "CBLCollection")
    CBLCollection_Release(t)
}

internal fun CBLDatabase_Release(t: CPointer<CBLDatabase>?) {
    RefTracker.trackRelease(t, "CBLDatabase")
    CBLDatabase_Release(t)
}

internal fun CBLDocument_Release(t: CPointer<CBLDocument>?) {
    RefTracker.trackRelease(t, "CBLDocument")
    CBLDocument_Release(t)
}

internal fun CBLEndpoint_Free(arg0: CPointer<CBLEndpoint>?) {
    RefTracker.trackRelease(arg0, "CBLEndpoint")
    CBLEndpoint_Free(arg0)
}

internal fun CBLQuery_Release(t: CPointer<CBLQuery>?) {
    RefTracker.trackRelease(t, "CBLQuery")
    CBLQuery_Release(t)
}

internal fun CBLQueryIndex_Release(t: CPointer<CBLQueryIndex>?) {
    RefTracker.trackRelease(t, "CBLQueryIndex")
    CBLQueryIndex_Release(t)
}

internal fun CBLReplicator_Release(t: CPointer<CBLReplicator>?) {
    RefTracker.trackRelease(t, "CBLReplicator")
    CBLReplicator_Release(t)
}

internal fun CBLResultSet_Release(t: CPointer<CBLResultSet>?) {
    RefTracker.trackRelease(t, "CBLResultSet")
    CBLResultSet_Release(t)
}

internal fun CBLScope_Release(t: CPointer<CBLScope>?) {
    RefTracker.trackRelease(t, "CBLScope")
    CBLScope_Release(t)
}

internal fun FLArray_Release(v: FLArray?) {
    RefTracker.trackRelease(v, "FLArray")
    FLArray_Release(v)
}

internal fun FLDict_Release(v: FLDict?) {
    RefTracker.trackRelease(v, "FLDict")
    FLDict_Release(v)
}

internal fun FLDoc_Release(arg0: FLDoc?) {
    RefTracker.trackRelease(arg0, "FLDoc")
    FLDoc_Release(arg0)
}

internal fun FLMutableArray_Release(d: FLMutableArray?) {
    RefTracker.trackRelease(d, "FLArray")
    FLMutableArray_Release(d)
}

internal fun FLMutableDict_Release(d: FLMutableDict?) {
    RefTracker.trackRelease(d, "FLDict")
    FLMutableDict_Release(d)
}

internal fun FLSliceResult_Release(s: CValue<FLSliceResult>) {
    RefTracker.trackRelease(s.useContents { buf }, "FLSliceResult")
    FLSliceResult_Release(s)
}

internal fun FLValue_Release(arg0: FLValue?) {
    RefTracker.trackRelease(arg0, "FLValue")
    FLValue_Release(arg0)
}

