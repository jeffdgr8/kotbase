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

import kotbase.internal.DelegatedClass
import kotbase.internal.actualSet
import com.couchbase.lite.URLEndpointListenerConfiguration as CBLURLEndpointListenerConfiguration

public actual class URLEndpointListenerConfiguration
private constructor(
    @Deprecated("Use collections")
    public actual val database: Database,
    public actual val collections: Set<Collection>,
    identity: TLSIdentity?,
    authenticator: ListenerAuthenticator?,
    actual: CBLURLEndpointListenerConfiguration
) : DelegatedClass<CBLURLEndpointListenerConfiguration>(actual) {

    @Suppress("DEPRECATION")
    @Deprecated(
        "Use URLEndpointListenerConfiguration(Collections)",
        ReplaceWith("URLEndpointListenerConfiguration(setOf(database.getDefaultCollection()), networkInterface, port, disableTls, identity, authenticator, readOnly, enableDeltaSync)")
    )
    public actual constructor(
        database: Database,
        networkInterface: String?,
        port: Int,
        disableTls: Boolean,
        identity: TLSIdentity?,
        authenticator: ListenerAuthenticator?,
        readOnly: Boolean,
        enableDeltaSync: Boolean
    ) : this(
        database,
        setOf(database.getDefaultCollectionNotNull()),
        identity,
        authenticator,
        CBLURLEndpointListenerConfiguration(database.actual).apply {
            this.networkInterface = networkInterface
            this.port = port
            this.setDisableTls(disableTls)
            this.tlsIdentity = identity?.actual
            this.authenticator = authenticator?.actual
            this.isReadOnly = readOnly
            this.setEnableDeltaSync(enableDeltaSync)
        }
    )

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
        collections.first().database,
        collections,
        identity,
        authenticator,
        CBLURLEndpointListenerConfiguration(
            collections.actualSet(),
            networkInterface,
            port,
            disableTls,
            identity?.actual,
            authenticator?.actual,
            readOnly,
            enableDeltaSync
        )
    )

    @Suppress("DEPRECATION")
    public actual constructor(config: URLEndpointListenerConfiguration) : this(
        config.database,
        config.collections,
        config.tlsIdentity,
        config.authenticator,
        CBLURLEndpointListenerConfiguration(config.actual)
    )

    public actual var networkInterface: String?
        get() = actual.networkInterface
        set(value) {
            actual.networkInterface = value
        }

    public actual var port: Int?
        get() = actual.port
        set(value) {
            actual.port = value ?: 0
        }

    public actual var isTlsDisabled: Boolean
        get() = actual.isTlsDisabled
        set(value) {
            actual.setDisableTls(value)
        }

    public actual var tlsIdentity: TLSIdentity? = identity
        set(value) {
            field = value
            actual.tlsIdentity = value?.actual
        }

    public actual var authenticator: ListenerAuthenticator? = authenticator
        set(value) {
            field = value
            actual.authenticator = value?.actual
        }

    public actual var isReadOnly: Boolean
        get() = actual.isReadOnly
        set(value) {
            actual.isReadOnly = value
        }

    public actual var isDeltaSyncEnabled: Boolean
        get() = actual.isDeltaSyncEnabled
        set(value) {
            actual.setEnableDeltaSync(value)
        }

    public actual companion object
}
