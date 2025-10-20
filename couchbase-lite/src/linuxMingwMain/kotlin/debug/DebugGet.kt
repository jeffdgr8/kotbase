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
import cnames.structs.CBLListenerToken
import cnames.structs.CBLQuery
import cnames.structs.CBLQueryIndex
import cnames.structs.CBLReplicator
import cnames.structs.CBLResultSet
import cnames.structs.CBLScope
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.useContents
import libcblite.CBLBlob_Content
import libcblite.CBLCollection_GetDocument
import libcblite.CBLCollection_GetIndex
import libcblite.CBLCollection_GetIndexNames
import libcblite.CBLCollection_GetIndexesInfo
import libcblite.CBLCollection_Scope
import libcblite.CBLDatabase_Collection
import libcblite.CBLDatabase_CollectionNames
import libcblite.CBLDatabase_DefaultCollection
import libcblite.CBLDatabase_DefaultScope
import libcblite.CBLDatabase_GetBlob
import libcblite.CBLDatabase_GetDocument
import libcblite.CBLDatabase_GetIndexNames
import libcblite.CBLDatabase_Path
import libcblite.CBLDatabase_Scope
import libcblite.CBLDatabase_ScopeNames
import libcblite.CBLDocument_Collection
import libcblite.CBLDocument_GetRevisionHistory
import libcblite.CBLError
import libcblite.CBLError_Message
import libcblite.CBLQuery_CopyCurrentResults
import libcblite.CBLQuery_Execute
import libcblite.CBLQuery_Explain
import libcblite.CBLReplicator_PendingDocumentIDs
import libcblite.CBLReplicator_PendingDocumentIDs2
import libcblite.CBLScope_Collection
import libcblite.CBLScope_CollectionNames
import libcblite.FLArray
import libcblite.FLDict
import libcblite.FLMutableArray
import libcblite.FLSliceResult
import libcblite.FLString
import libcblite.FLStringResult

// Regex to find create calls not from this package:
// (?<!\.)\b\w+_Get
// Potentially others...

internal fun CBLBlob_Content(blob: CValuesRef<CBLBlob>?, outError: CValuesRef<CBLError>?): CValue<FLSliceResult> {
    return CBLBlob_Content(blob, outError).also {
        RefTracker.trackRetain(it.useContents { buf }, "FLSliceResult")
    }
}

internal fun CBLCollection_Scope(collection: CValuesRef<CBLCollection>?): CPointer<CBLScope>? {
    return CBLCollection_Scope(collection).also {
        RefTracker.trackRetain(it, "CBLScope")
    }
}

internal fun CBLCollection_GetDocument(collection: CValuesRef<CBLCollection>?, docID: CValue<FLString>, outError: CValuesRef<CBLError>?): CPointer<CBLDocument>? {
    return CBLCollection_GetDocument(collection, docID, outError).also {
        RefTracker.trackRetain(it, "CBLDocument")
    }
}

internal fun CBLCollection_GetIndex(collection: CValuesRef<CBLCollection>?, name: CValue<FLString>, outError: CValuesRef<CBLError>?): CPointer<CBLQueryIndex>? {
    return CBLCollection_GetIndex(collection, name, outError).also {
        RefTracker.trackRetain(it, "CBLQueryIndex")
    }
}

internal fun CBLCollection_GetIndexesInfo(collection: CValuesRef<CBLCollection>?, outError: CValuesRef<CBLError>?): FLMutableArray? {
    return CBLCollection_GetIndexesInfo(collection, outError).also {
        RefTracker.trackRetain(it, "FLArray")
    }
}

internal fun CBLCollection_GetIndexNames(collection: CValuesRef<CBLCollection>?, outError: CValuesRef<CBLError>?): FLMutableArray? {
    return CBLCollection_GetIndexNames(collection, outError).also {
        RefTracker.trackRetain(it, "FLArray")
    }
}

internal fun CBLDatabase_Collection(db: CValuesRef<CBLDatabase>?, collectionName: CValue<FLString>, scopeName: CValue<FLString>, outError: CValuesRef<CBLError>?): CPointer<CBLCollection>? {
    return CBLDatabase_Collection(db, collectionName, scopeName, outError).also {
        RefTracker.trackRetain(it, "CBLCollection")
    }
}

internal fun CBLDatabase_CollectionNames(db: CValuesRef<CBLDatabase>?, scopeName: CValue<FLString>, outError: CValuesRef<CBLError>?): FLMutableArray? {
    return CBLDatabase_CollectionNames(db, scopeName, outError).also {
        RefTracker.trackRetain(it, "FLArray")
    }
}

internal fun CBLDatabase_DefaultCollection(db: CValuesRef<CBLDatabase>?, outError: CValuesRef<CBLError>?): CPointer<CBLCollection>? {
    return CBLDatabase_DefaultCollection(db, outError).also {
        RefTracker.trackRetain(it, "CBLCollection")
    }
}

internal fun CBLDatabase_DefaultScope(db: CValuesRef<CBLDatabase>?, outError: CValuesRef<CBLError>?): CPointer<CBLScope>? {
    return CBLDatabase_DefaultScope(db, outError).also {
        RefTracker.trackRetain(it, "CBLScope")
    }
}

internal fun CBLDatabase_GetBlob(db: CValuesRef<CBLDatabase>?, properties: FLDict?, outError: CValuesRef<CBLError>?): CPointer<CBLBlob>? {
    return CBLDatabase_GetBlob(db, properties, outError).also {
        RefTracker.trackRetain(it, "CBLBlob")
    }
}

internal fun CBLDatabase_GetDocument(database: CValuesRef<CBLDatabase>?, docID: CValue<FLString>, outError: CValuesRef<CBLError>?): CPointer<CBLDocument>? {
    return CBLDatabase_GetDocument(database, docID, outError).also {
        RefTracker.trackRetain(it, "CBLDocument")
    }
}

internal fun CBLDatabase_GetIndexNames(db: CValuesRef<CBLDatabase>?): FLArray? {
    return CBLDatabase_GetIndexNames(db).also {
        RefTracker.trackRetain(it, "FLArray")
    }
}

internal fun CBLDatabase_Path(arg0: CValuesRef<CBLDatabase>?): CValue<FLStringResult> {
    return CBLDatabase_Path(arg0).also {
        RefTracker.trackRetain(it.useContents { buf }, "FLSliceResult")
    }
}

internal fun CBLDatabase_Scope(db: CValuesRef<CBLDatabase>?, scopeName: CValue<FLString>, outError: CValuesRef<CBLError>?): CPointer<CBLScope>? {
    return CBLDatabase_Scope(db, scopeName, outError).also {
        RefTracker.trackRetain(it, "CBLScope")
    }
}

internal fun CBLDatabase_ScopeNames(db: CValuesRef<CBLDatabase>?, outError: CValuesRef<CBLError>?): FLMutableArray? {
    return CBLDatabase_ScopeNames(db, outError).also {
        RefTracker.trackRetain(it, "FLArray")
    }
}

internal fun CBLDocument_GetRevisionHistory(doc: CValuesRef<CBLDocument>?): CValue<FLSliceResult> {
    return CBLDocument_GetRevisionHistory(doc).also {
        RefTracker.trackRetain(it.useContents { buf }, "FLSliceResult")
    }
}

internal fun CBLError_Message(outError: CValuesRef<CBLError>?): CValue<FLSliceResult> {
    return CBLError_Message(outError).also {
        RefTracker.trackRetain(it.useContents { buf }, "FLSliceResult")
    }
}

internal fun CBLQuery_CopyCurrentResults(query: CValuesRef<CBLQuery>?, listener: CValuesRef<CBLListenerToken>?, outError: CValuesRef<CBLError>?): CPointer<CBLResultSet>? {
    return CBLQuery_CopyCurrentResults(query, listener, outError).also {
        RefTracker.trackRetain(it, "CBLResultSet")
    }
}

internal fun CBLQuery_Execute(arg0: CValuesRef<CBLQuery>?, outError: CValuesRef<CBLError>?): CPointer<CBLResultSet>? {
    return CBLQuery_Execute(arg0, outError).also {
        RefTracker.trackRetain(it, "CBLResultSet")
    }
}

internal fun CBLQuery_Explain(arg0: CValuesRef<CBLQuery>?): CValue<FLSliceResult> {
    return CBLQuery_Explain(arg0).also {
        RefTracker.trackRetain(it.useContents { buf }, "FLSliceResult")
    }
}

internal fun CBLReplicator_PendingDocumentIDs(arg0: CValuesRef<CBLReplicator>?, outError: CValuesRef<CBLError>?): FLDict? {
    return CBLReplicator_PendingDocumentIDs(arg0, outError).also {
        RefTracker.trackRetain(it, "FLDict")
    }
}

internal fun CBLReplicator_PendingDocumentIDs2(arg0: CValuesRef<CBLReplicator>?, collection: CValuesRef<CBLCollection>?, outError: CValuesRef<CBLError>?): FLDict? {
    return CBLReplicator_PendingDocumentIDs2(arg0, collection, outError).also {
        RefTracker.trackRetain(it, "FLDict")
    }
}

internal fun CBLScope_Collection(scope: CValuesRef<CBLScope>?, collectionName: CValue<FLString>, outError: CValuesRef<CBLError>?): CPointer<CBLCollection>? {
    return CBLScope_Collection(scope, collectionName, outError).also {
        RefTracker.trackRetain(it, "CBLCollection")
    }
}

internal fun CBLScope_CollectionNames(scope: CValuesRef<CBLScope>?, outError: CValuesRef<CBLError>?): FLMutableArray? {
    return CBLScope_CollectionNames(scope, outError).also {
        RefTracker.trackRetain(it, "FLArray")
    }
}
