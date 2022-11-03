package com.couchbase.lite.kmp

public actual class URLEndpointListenerConfiguration private constructor() {

    init {
        urlEndpointListenerUnsupported()
    }

    public actual constructor(
        database: Database,
        networkInterface: String?,
        port: Int,
        disableTls: Boolean,
        identity: TLSIdentity?,
        authenticator: ListenerAuthenticator?,
        readOnly: Boolean,
        enableDeltaSync: Boolean
    ) : this()

    public actual constructor(config: URLEndpointListenerConfiguration) : this()

    public actual val database: Database

    public actual var networkInterface: String?

    public actual var port: Int

    public actual var isTlsDisabled: Boolean

    public actual var tlsIdentity: TLSIdentity?

    public actual var authenticator: ListenerAuthenticator?

    public actual var isReadOnly: Boolean

    public actual var isDeltaSyncEnabled: Boolean
}
