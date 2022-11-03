package com.couchbase.lite.kmp

/**
 * Configuration for MessageEndpointListener
 */
public expect class MessageEndpointListenerConfiguration(
    database: Database,
    protocolType: ProtocolType
) {

    public val database: Database

    public val protocolType: ProtocolType
}
