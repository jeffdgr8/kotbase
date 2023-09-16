/*
 * Copyright 2022-2023 Jeff Lockhart
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
package kotbase.internal

import kotbase.*

internal actual class DbContext(var database: Database?) {

    // need to avoid calling expensive Blob.hashCode()
    // so using === comparison with List instead of Set
    private val streamBlobs = mutableListOf<Blob>()

    // can't unify with internal interface without making mergeSavedBlobs() public
    private val streamBlobDocs = mutableListOf<MutableDocument>()
    private val streamBlobDicts = mutableListOf<MutableDictionary>()
    private val streamBlobArrays = mutableListOf<MutableArray>()

    internal fun addStreamBlob(blob: Blob) {
        streamBlobs.identityAdd(blob)
    }

    internal fun addStreamBlobDoc(doc: MutableDocument) {
        streamBlobDocs.identityAdd(doc)
    }

    internal fun removeStreamBlobDoc(doc: MutableDocument) {
        streamBlobDocs.identityRemove(doc)
    }

    internal fun addStreamBlobDict(dict: MutableDictionary) {
        streamBlobDicts.identityAdd(dict)
    }

    internal fun removeStreamBlobDict(dict: MutableDictionary) {
        streamBlobDicts.identityRemove(dict)
    }

    internal fun addStreamBlobArray(array: MutableArray) {
        streamBlobArrays.identityAdd(array)
    }

    internal fun removeStreamBlobArray(array: MutableArray) {
        streamBlobArrays.identityRemove(array)
    }

    private fun <T> MutableList<T>.identityAdd(value: T) {
        find { it === value } ?: add(value)
    }

    private fun <T> MutableList<T>.identityRemove(value: T) {
        // avoid calling equals() with remove()
        run loop@{
            forEachIndexed { i, v ->
                if (value === v) {
                    removeAt(i)
                    return@loop
                }
            }
        }
    }

    internal fun willSave(db: Database) {
        streamBlobs.forEach { blob ->
            blob.saveToDb(db)
        }
        streamBlobs.clear()
        streamBlobDocs.forEach { doc ->
            doc.mergeSavedBlobs()
        }
        streamBlobDocs.clear()
        streamBlobDicts.forEach { dict ->
            dict.mergeSavedBlobs()
        }
        streamBlobDicts.clear()
        streamBlobArrays.forEach { array ->
            array.mergeSavedBlobs()
        }
        streamBlobArrays.clear()
    }
}
