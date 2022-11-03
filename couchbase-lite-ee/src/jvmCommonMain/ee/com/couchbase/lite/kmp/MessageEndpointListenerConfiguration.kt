package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class MessageEndpointListenerConfiguration
internal constructor(
    actual: com.couchbase.lite.MessageEndpointListenerConfiguration,
    public actual val database: Database
) : DelegatedClass<com.couchbase.lite.MessageEndpointListenerConfiguration>(actual) {

    public actual constructor(
        database: Database,
        protocolType: ProtocolType
    ) : this(
        com.couchbase.lite.MessageEndpointListenerConfiguration(database.actual, protocolType),
        database
    )

    public actual val protocolType: ProtocolType
        get() = actual.protocolType
}
