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
package kotbase

import cnames.structs.CBLCollection
import kotlinx.cinterop.*
import libcblite.CBLURLEndpointListenerConfiguration
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual class URLEndpointListenerConfiguration
private constructor(
    public actual val collections: Set<Collection>,
    public actual var networkInterface: String?,
    port: Int?,
    public actual var isTlsDisabled: Boolean,
    public actual var tlsIdentity: TLSIdentity?,
    public actual var authenticator: ListenerAuthenticator?,
    public actual var isReadOnly: Boolean,
    public actual var isDeltaSyncEnabled: Boolean
) {

    init {
        require(collections.isNotEmpty()) {
            "Listener configuration must specify at least one collection"
        }
        val database = collections.first().database
        require(collections.all { it.database == database }) {
            "Cannot create a listener configuration for collections from different databases"
        }
    }

    public actual constructor(
        collections: Set<Collection>,
        networkInterface: String?,
        port: Int,
        disableTls: Boolean,
        identity: TLSIdentity?,
        authenticator: ListenerAuthenticator?,
        readOnly: Boolean,
        enableDeltaSync: Boolean
    ) : this(
        collections.toSet(),
        networkInterface,
        port as Int?,
        disableTls,
        identity,
        authenticator,
        readOnly,
        enableDeltaSync
    )

    public actual constructor(config: URLEndpointListenerConfiguration) : this(
        config.collections,
        config.networkInterface,
        config.port,
        config.isTlsDisabled,
        config.tlsIdentity,
        config.authenticator,
        config.isReadOnly,
        config.isDeltaSyncEnabled
    )

    internal constructor(config: ImmutableURLEndpointListenerConfiguration) : this(
        config.collections,
        config.networkInterface,
        config.port,
        config.isTlsDisabled,
        config.tlsIdentity,
        config.authenticator,
        config.isReadOnly,
        config.isDeltaSyncEnabled
    )

    public actual var port: Int? = port
        set(value) {
            if (value != null) {
                require(value in MIN_PORT..MAX_PORT) {
                    "The port must be between $MIN_PORT and $MAX_PORT inclusive"
                }
            }
            field = value
        }

    public actual companion object
}

internal class ImmutableURLEndpointListenerConfiguration(config: URLEndpointListenerConfiguration) {

    private val memory = object {
        val arena = Arena()
    }

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        it.arena.clear()
    }

    val collections: Set<Collection> = config.collections.toSet()
    val networkInterface: String? = config.networkInterface
    val port: Int? = config.port
    val isTlsDisabled: Boolean = config.isTlsDisabled
    val tlsIdentity: TLSIdentity? = config.tlsIdentity
    val authenticator: ListenerAuthenticator? = config.authenticator
    val isReadOnly: Boolean = config.isReadOnly
    val isDeltaSyncEnabled: Boolean = config.isDeltaSyncEnabled

    val actual: CPointer<CBLURLEndpointListenerConfiguration> =
        memory.arena.alloc<CBLURLEndpointListenerConfiguration>().also {
            val collectionList = collections.toList()
            it.collectionCount = collectionList.size.convert()
            it.collections = memory.arena.allocArray<CPointerVar<CBLCollection>>(collectionList.size).also { array ->
                collectionList.forEachIndexed { index, collection ->
                    array[index] = collection.actual
                }
            }
            it.port = (port ?: 0).convert()
            networkInterface?.encodeToByteArray()?.let { bytes ->
                it.networkInterface.apply {
                    buf = memory.arena.allocArrayOf(bytes)
                    size = bytes.size.convert()
                }
            }
            it.disableTLS = isTlsDisabled
            it.tlsIdentity = tlsIdentity?.actual
            it.authenticator = authenticator?.actual
            it.readOnly = isReadOnly
            it.enableDeltaSync = isDeltaSyncEnabled
        }.ptr
}
