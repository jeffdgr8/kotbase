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
import cnames.structs.CBLBlobWriteStream
import cnames.structs.CBLCollection
import cnames.structs.CBLDatabase
import cnames.structs.CBLDocument
import cnames.structs.CBLEndpoint
import cnames.structs.CBLQuery
import cnames.structs.CBLReplicator
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.useContents
import libcblite.CBLAuth_CreatePassword
import libcblite.CBLAuth_CreateSession
import libcblite.CBLBlob_CreateWithData
import libcblite.CBLBlob_CreateWithStream
import libcblite.CBLDatabaseConfiguration
import libcblite.CBLDatabase_CreateCollection
import libcblite.CBLDatabase_CreateQuery
import libcblite.CBLDatabase_Open
import libcblite.CBLDocument_Create
import libcblite.CBLDocument_CreateJSON
import libcblite.CBLDocument_CreateWithID
import libcblite.CBLDocument_MutableCopy
import libcblite.CBLEndpoint_CreateWithURL
import libcblite.CBLError
import libcblite.CBLQueryLanguage
import libcblite.CBLReplicatorConfiguration
import libcblite.CBLReplicator_Create
import libcblite.FLArray
import libcblite.FLArray_MutableCopy
import libcblite.FLCopyFlags
import libcblite.FLDict
import libcblite.FLDict_MutableCopy
import libcblite.FLDoc
import libcblite.FLDoc_FromJSON
import libcblite.FLErrorVar
import libcblite.FLMutableArray
import libcblite.FLMutableArray_New
import libcblite.FLMutableArray_NewFromJSON
import libcblite.FLMutableDict
import libcblite.FLMutableDict_New
import libcblite.FLMutableDict_NewFromJSON
import libcblite.FLSlice
import libcblite.FLSliceResult
import libcblite.FLString
import libcblite.FLStringResult
import libcblite.FLValue
import libcblite.FLValue_ToJSON

// Regex to find create calls not from this package:
// (?<!\.)\b\w+_Create
// (?<!\.)\b\w+_New
// (?<!\.)\b\w+_MutableCopy\b

internal fun CBLAuth_CreatePassword(username: CValue<FLString>, password: CValue<FLString>): CPointer<CBLAuthenticator>? {
    return CBLAuth_CreatePassword(username, password).also {
        RefTracker.trackRetain(it, "CBLAuthenticator")
    }
}

internal fun CBLAuth_CreateSession(sessionID: CValue<FLString>, cookieName: CValue<FLString>): CPointer<CBLAuthenticator>? {
    return CBLAuth_CreateSession(sessionID, cookieName).also {
        RefTracker.trackRetain(it, "CBLAuthenticator")
    }
}

internal fun CBLBlob_CreateWithData(contentType: CValue<FLString>, contents: CValue<FLSlice>): CPointer<CBLBlob>? {
    return CBLBlob_CreateWithData(contentType, contents).also {
        RefTracker.trackRetain(it, "CBLBlob")
    }
}

internal fun CBLBlob_CreateWithStream(contentType: CValue<FLString>, writer: CValuesRef<CBLBlobWriteStream>?): CPointer<CBLBlob>? {
    return CBLBlob_CreateWithStream(contentType, writer).also {
        RefTracker.trackRetain(it, "CBLBlob")
    }
}

internal fun CBLDatabase_CreateCollection(db: CValuesRef<CBLDatabase>?, collectionName: CValue<FLString>, scopeName: CValue<FLString>, outError: CValuesRef<CBLError>?): CPointer<CBLCollection>? {
    return CBLDatabase_CreateCollection(db, collectionName, scopeName, outError).also {
        RefTracker.trackRetain(it, "CBLCollection")
    }
}

internal fun CBLDatabase_CreateQuery(db: CValuesRef<CBLDatabase>?, language: CBLQueryLanguage, queryString: CValue<FLString>, outErrorPos: CValuesRef<IntVar>?, outError: CValuesRef<CBLError>?): CPointer<CBLQuery>? {
    return CBLDatabase_CreateQuery(db, language, queryString, outErrorPos, outError).also {
        RefTracker.trackRetain(it, "CBLQuery")
    }
}

internal fun CBLDatabase_Open(name: CValue<FLSlice>, config: CValuesRef<CBLDatabaseConfiguration>?, outError: CValuesRef<CBLError>?): CPointer<CBLDatabase>? {
    return CBLDatabase_Open(name, config, outError).also {
        RefTracker.trackRetain(it, "CBLDatabase")
    }
}

internal fun CBLDocument_Create(): CPointer<CBLDocument>? {
    return CBLDocument_Create().also {
        RefTracker.trackRetain(it, "CBLDocument")
    }
}

internal fun CBLDocument_CreateJSON(arg0: CValuesRef<CBLDocument>?): CValue<FLSliceResult> {
    return CBLDocument_CreateJSON(arg0).also {
        RefTracker.trackRetain(it.useContents { buf }, "FLSliceResult")
    }
}

internal fun CBLDocument_CreateWithID(docID: CValue<FLString>): CPointer<CBLDocument>? {
    return CBLDocument_CreateWithID(docID).also {
        RefTracker.trackRetain(it, "CBLDocument")
    }
}

internal fun CBLDocument_MutableCopy(original: CValuesRef<CBLDocument>?): CPointer<CBLDocument>? {
    return CBLDocument_MutableCopy(original).also {
        RefTracker.trackRetain(it, "CBLDocument")
    }
}

internal fun CBLEndpoint_CreateWithURL(url: CValue<FLString>, outError: CValuesRef<CBLError>?): CPointer<CBLEndpoint>? {
    return CBLEndpoint_CreateWithURL(url, outError).also {
        RefTracker.trackRetain(it, "CBLEndpoint")
    }
}

internal fun CBLReplicator_Create(arg0: CValuesRef<CBLReplicatorConfiguration>?, outError: CValuesRef<CBLError>?): CPointer<CBLReplicator>? {
    return CBLReplicator_Create(arg0, outError).also {
        RefTracker.trackRetain(it, "CBLReplicator")
    }
}

internal fun FLArray_MutableCopy(arg0: FLArray?, arg1: FLCopyFlags): FLMutableArray? {
    return FLArray_MutableCopy(arg0, arg1).also {
        RefTracker.trackRetain(it, "FLArray")
    }
}

internal fun FLDict_MutableCopy(source: FLDict?, arg1: FLCopyFlags): FLMutableDict? {
    return FLDict_MutableCopy(source, arg1).also {
        RefTracker.trackRetain(it, "FLDict")
    }
}

internal fun FLDoc_FromJSON(json: CValue<FLSlice>, outError: CValuesRef<FLErrorVar>?): FLDoc? {
    return FLDoc_FromJSON(json, outError).also {
        RefTracker.trackRetain(it, "FLDoc")
    }
}

internal fun FLMutableArray_New(): FLMutableArray? {
    return FLMutableArray_New().also {
        RefTracker.trackRetain(it, "FLArray")
    }
}

internal fun FLMutableArray_NewFromJSON(json: CValue<FLString>, outError: CValuesRef<FLErrorVar>?): FLMutableArray? {
    return FLMutableArray_NewFromJSON(json, outError).also {
        RefTracker.trackRetain(it, "FLArray")
    }
}

internal fun FLMutableDict_New(): FLMutableDict? {
    return FLMutableDict_New().also {
        RefTracker.trackRetain(it, "FLDict")
    }
}

internal fun FLMutableDict_NewFromJSON(json: CValue<FLString>, outError: CValuesRef<FLErrorVar>?): FLMutableDict? {
    return FLMutableDict_NewFromJSON(json, outError).also {
        RefTracker.trackRetain(it, "FLDict")
    }
}

internal fun FLValue_ToJSON(arg0: FLValue?): CValue<FLStringResult> {
    return FLValue_ToJSON(arg0).also {
        RefTracker.trackRetain(it.useContents { buf }, "FLSliceResult")
    }
}
