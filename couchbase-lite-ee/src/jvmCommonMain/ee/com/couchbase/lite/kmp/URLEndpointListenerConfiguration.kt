package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class URLEndpointListenerConfiguration
private constructor(
    public actual val database: Database,
    identity: TLSIdentity?,
    authenticator: ListenerAuthenticator?,
    actual: com.couchbase.lite.URLEndpointListenerConfiguration
) : DelegatedClass<com.couchbase.lite.URLEndpointListenerConfiguration>(actual) {

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
        com.couchbase.lite.URLEndpointListenerConfiguration(
            database.actual,
            networkInterface,
            port,
            disableTls,
            identity?.actual,
            authenticator?.actual,
            readOnly,
            enableDeltaSync
        )
    )

    public actual constructor(config: URLEndpointListenerConfiguration) : this(
        config.database,
        config.tlsIdentity,
        config.authenticator,
        com.couchbase.lite.URLEndpointListenerConfiguration(config.actual)
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
}
