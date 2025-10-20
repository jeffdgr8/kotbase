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
@file:OptIn(ExperimentalNativeApi::class)

package debug

import cnames.structs.CBLCollection
import cnames.structs.CBLDocument
import kotbase.BasicAuthenticator
import kotbase.Collection
import kotbase.Database
import kotbase.DatabaseConfiguration
import kotbase.Replicator
import kotbase.ReplicatorActivityLevel
import kotbase.ReplicatorConfiguration
import kotbase.ReplicatorType
import kotbase.URLEndpoint
import kotbase.ext.toStringMillis
import kotbase.internal.fleece.setDate
import kotbase.internal.fleece.toFLString
import kotbase.internal.fleece.toKString
import kotbase.internal.fleece.toList
import kotbase.internal.wrapCBLError
import kotbase.replicatorChangesFlow
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.cValue
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import libcblite.CBLCollection_CreateValueIndex
import libcblite.CBLCollection_GetDocument
import libcblite.CBLCollection_SaveDocument
import libcblite.CBLDatabase_DefaultCollection
import libcblite.CBLDatabase_Open
import libcblite.CBLDocument_CreateWithID
import libcblite.CBLDocument_ID
import libcblite.CBLDocument_MutableProperties
import libcblite.CBLDocument_Properties
import libcblite.CBLDocument_Release
import libcblite.CBL_DeleteDatabase
import libcblite.FLArray
import libcblite.FLArray_Release
import libcblite.FLDict
import libcblite.FLDict_Get
import libcblite.FLDict_Release
import libcblite.FLMutableArray_AppendDict
import libcblite.FLMutableArray_AppendString
import libcblite.FLMutableArray_New
import libcblite.FLMutableArray_Release
import libcblite.FLMutableDict
import libcblite.FLMutableDict_New
import libcblite.FLMutableDict_SetArray
import libcblite.FLMutableDict_SetInt
import libcblite.FLMutableDict_SetString
import libcblite.FLValue_AsArray
import libcblite.FLValue_AsInt
import libcblite.FLValue_AsString
import libcblite.kCBLN1QLLanguage
import platform.posix.strdup
import platform.posix.strlen
import kotlin.experimental.ExperimentalNativeApi

/**
 * Reference tracker for debugging native memory management issues.
 *
 * * Replace calls to the Couchbase Lite C SDK API with the equivalent functions
 * in the debug package to track object retain and release in ref-tracker.cblite2.
 *
 * * Set `TRACK = true` to track references when running tests.
 *
 * * Optionally set Sync Gateway URL and credentials, set `TRACK = false`, and call
 * `pushSync()` to sync results to Couchbase Server.
 *
 * **Note:** This class intentionally does not reference or use the Kotbase API for its
 * database use to avoid cyclical references and infinite recursion.
 * Similarly, calls to the Couchbase Lite API should not go through the debug package.
 */
internal object RefTracker {

    private const val TRACK = false
    private const val INCLUDE_STACK_TRACES = true

    // Optional Sync Gateway config
    private const val URL_ENDPOINT = "ws://127.0.0.1:4984/db"
    private const val USERNAME = "user"
    private const val PASSWORD = "password"

    private const val DB_NAME = "ref-tracker"

    init {
        if (TRACK) {
            wrapCBLError { error ->
                memScoped {
                    CBL_DeleteDatabase(
                        DB_NAME.toFLString(this),
                        null.toFLString(this),
                        error
                    )
                }
            }
        }
    }

    private val database = wrapCBLError { error ->
        memScoped {
            CBLDatabase_Open(DB_NAME.toFLString(this), null, error)
        }
    }!!
    private val collection = wrapCBLError { error ->
        CBLDatabase_DefaultCollection(database, error)
    }!!
    private val lock = reentrantLock()

    fun trackRetain(ref: CPointer<*>?, type: String) {
        if (!TRACK || ref == null) return

        lock.withLock {
            val id = ref.rawValue.toString()
            val ref = collection.getDocRef(id, type)
            ref.refCount++
            ref.lastEvent = Clock.System.now()
            if (INCLUDE_STACK_TRACES) ref.retains.add(Event())
            val doc = ref.toMutableDocument()
            collection.save(doc)
            CBLDocument_Release(doc)
        }
    }

    fun trackRelease(ref: CPointer<*>?, type: String) {
        if (!TRACK || ref == null) return

        lock.withLock {
            val id = ref.rawValue.toString()
            val ref = collection.getDocRef(id, type)
            ref.refCount--
            ref.lastEvent = Clock.System.now()
            if (INCLUDE_STACK_TRACES) ref.releases.add(Event())
            val doc = ref.toMutableDocument()
            collection.save(doc)
            CBLDocument_Release(doc)
        }
        println("Releasing $ref")
    }

    fun trackInit(ref: CPointer<*>?, type: String) {
        if (!TRACK || ref == null) return

        lock.withLock {
            val id = ref.rawValue.toString()
            val ref = collection.getDocRef(id, type)
            ref.lastEvent = Clock.System.now()
            if (INCLUDE_STACK_TRACES) ref.inits.add(Event())
            val doc = ref.toMutableDocument()
            collection.save(doc)
            CBLDocument_Release(doc)
        }
    }

    fun pushSync() {
        if (!TRACK) {
            println("Pushing debug ref tracker docs to server...")
            val config = ReplicatorConfiguration(URLEndpoint(URL_ENDPOINT))
                .setType(ReplicatorType.PUSH)
                .setContinuous(false)
                .setAuthenticator(BasicAuthenticator(USERNAME, PASSWORD.toCharArray()))
                .addCollection(Collection(collection, Database(this@RefTracker.database, DatabaseConfiguration())))
            val replicator = Replicator(config)
            val changeFlow = replicator.replicatorChangesFlow(Dispatchers.Default)
            replicator.start()
            runBlocking {
                launch {
                    changeFlow.collect {
                        it.status.error?.let { print(it) }
                        with(it.status) {
                            println("$activityLevel: ${progress.completed}/${progress.total}")
                            if (activityLevel == ReplicatorActivityLevel.STOPPED) {
                                cancel()
                            }
                        }
                    }
                }
            }
            println("Done")
        } else {
            throw IllegalStateException("Tracking is enabled! Disable before syncing results to server.")
        }
    }
}

internal data class Ref(
    val id: String,
    val type: String,
    var refCount: Int = 0,
    var lastEvent: Instant = Clock.System.now(),
    val inits: MutableList<Event> = mutableListOf(),
    val retains: MutableList<Event> = mutableListOf(),
    val releases: MutableList<Event> = mutableListOf()
)

internal data class Event(
    val timestamp: Instant = Clock.System.now(),
    val stackTrace: List<String> = Throwable().getStackTrace().filter {
        !it.contains("/debug/") && it.contains("kotbase")
    }.map {
        var start = it.indexOf("kfun:")
        if (start == -1) {
            val blankBefore = "           "
            start = it.lastIndexOf(blankBefore) + blankBefore.length
        }
        it.substring(start)
            .replace("/home/jeff/Documents/GitHub/kotbase/", "")
    }
)

private fun CPointer<CBLCollection>.createIndex(name: String, vararg expressions: String) {
    wrapCBLError { error ->
        memScoped {
            val exp = expressions.joinToString(separator = ",")
            CBLCollection_CreateValueIndex(
                this@createIndex,
                name.toFLString(this),
                cValue {
                    expressionLanguage = kCBLN1QLLanguage
                    this@cValue.expressions.buf = strdup(exp)
                    this@cValue.expressions.size = strlen(exp)
                },
                error
            )
        }
    }
}

private fun CPointer<CBLCollection>.getDocRef(id: String, type: String): Ref {
    return wrapCBLError { error ->
        memScoped {
            var doc: CPointer<CBLDocument>? = CBLCollection_GetDocument(this@getDocRef, id.toFLString(this), error)
            var num = 0
            while (doc != null && doc.getType() != type) {
                doc = CBLCollection_GetDocument(this@getDocRef, "$id-${++num}".toFLString(this), error)
            }
            doc?.toRef() ?: Ref(if (num == 0) id else "$id-$num", type)
        }
    }
}

private fun CPointer<CBLCollection>.save(doc: CPointer<CBLDocument>) {
    wrapCBLError { error ->
        CBLCollection_SaveDocument(this, doc, error)
    }
}

private fun CPointer<CBLDocument>.getType(): String {
    val props = CBLDocument_Properties(this)
    return memScoped {
        FLValue_AsString(FLDict_Get(props, "type".toFLString(this))).toKString()!!
    }
}

internal fun CPointer<CBLDocument>.toRef(): Ref {
    val props = CBLDocument_Properties(this)
    val type = getType()
    val id = CBLDocument_ID(this).toKString()!!
    val refCount = memScoped {
        FLValue_AsInt(
            FLDict_Get(props, "refCount".toFLString(this))
        ).toInt()
    }
    val lastEvent = memScoped {
        Instant.parse(
            FLValue_AsString(
                FLDict_Get(props, "lastEvent".toFLString(this))
            ).toKString()!!
        )
    }
    val inits = props.getArray("inits")
    val retains = props.getArray("retains")
    val releases = props.getArray("releases")
    CBLDocument_Release(this)
    return Ref(
        id,
        type,
        refCount,
        lastEvent,
        inits,
        retains,
        releases
    )
}

private fun FLDict?.getArray(key: String): MutableList<Event> {
    return memScoped {
        FLValue_AsArray(
            FLDict_Get(this@getArray, key.toFLString(this))
        )?.toList(null)
    }?.map {
        @Suppress("UNCHECKED_CAST")
        (it as Map<String, Any?>).toEvent()
    }?.toMutableList() ?: mutableListOf()
}

internal fun Map<String, Any?>.toEvent(): Event {
    @Suppress("UNCHECKED_CAST")
    return Event(
        timestamp = Instant.parse(this["timestamp"] as String),
        stackTrace = this["stackTrace"] as List<String>
    )
}

internal fun Ref.toMutableDocument(): CPointer<CBLDocument> {
    return memScoped {
        val doc = CBLDocument_CreateWithID(id.toFLString(this))!!
        val props = CBLDocument_MutableProperties(doc)
        FLMutableDict_SetString(props, "type".toFLString(this), type.toFLString(this))
        FLMutableDict_SetInt(props, "refCount".toFLString(this), refCount.convert())
        FLMutableDict_SetString(props, "lastEvent".toFLString(this), lastEvent.toStringMillis().toFLString(this))
        props.setArray("inits", inits, this)
        props.setArray("retains", retains, this)
        props.setArray("releases", releases, this)
        doc
    }
}

private fun FLMutableDict?.setArray(key: String, value: List<Event>, scope: MemScope) {
    if (value.isNotEmpty()) {
        val array = value.toFLArray(scope)
        FLMutableDict_SetArray(this, key.toFLString(scope), array)
        FLMutableArray_Release(array)
    }
}

private fun List<Event>.toFLArray(scope: MemScope): FLArray {
    return FLMutableArray_New()!!.apply {
        forEach {
            val dict = it.toFLDict(scope)
            FLMutableArray_AppendDict(this@apply, dict)
            FLDict_Release(dict)
        }
    }
}

private fun Event.toFLDict(scope: MemScope): FLDict{
    return FLMutableDict_New()!!.apply {
        setDate("timestamp", timestamp)
        val array = stackTrace.toFLArray(scope)
        FLMutableDict_SetArray(this, "stackTrace".toFLString(scope), array)
        FLArray_Release(array)
    }
}

private fun List<String>.toFLArray(scope: MemScope): FLArray {
    return FLMutableArray_New()!!.apply {
        forEach {
            FLMutableArray_AppendString(this@apply, it.toFLString(scope))
        }
    }
}
