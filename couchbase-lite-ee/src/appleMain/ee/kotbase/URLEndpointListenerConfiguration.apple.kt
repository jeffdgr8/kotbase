package kotbase

import cocoapods.CouchbaseLite.CBLURLEndpointListenerConfiguration
import kotbase.base.DelegatedClass

public actual class URLEndpointListenerConfiguration
private constructor(
    public actual val database: Database,
    identity: TLSIdentity?,
    authenticator: ListenerAuthenticator?,
    actual: CBLURLEndpointListenerConfiguration
) : DelegatedClass<CBLURLEndpointListenerConfiguration>(actual) {

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
        identity,
        authenticator,
        CBLURLEndpointListenerConfiguration(database.actual).apply {
            this.networkInterface = networkInterface
            this.port = port.toUShort()
            this.disableTLS = disableTls
            this.tlsIdentity = identity?.actual
            this.authenticator = authenticator?.actual
            this.readOnly = readOnly
            this.enableDeltaSync = enableDeltaSync
        }
    )

    public actual constructor(config: URLEndpointListenerConfiguration) : this(
        config.database,
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
        get() = actual.port.toInt()
        set(value) {
            actual.port = value?.toUShort() ?: 0U
        }

    public actual var isTlsDisabled: Boolean
        get() = actual.disableTLS
        set(value) {
            actual.disableTLS = value
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
        get() = actual.readOnly
        set(value) {
            actual.readOnly = value
        }

    public actual var isDeltaSyncEnabled: Boolean
        get() = actual.enableDeltaSync
        set(value) {
            actual.enableDeltaSync = value
        }
}
