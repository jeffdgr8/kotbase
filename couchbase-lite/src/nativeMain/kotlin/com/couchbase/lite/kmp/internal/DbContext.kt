package com.couchbase.lite.kmp.internal

import com.couchbase.lite.kmp.*

internal class DbContext(var database: Database?) {

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
        run loop@ {
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
