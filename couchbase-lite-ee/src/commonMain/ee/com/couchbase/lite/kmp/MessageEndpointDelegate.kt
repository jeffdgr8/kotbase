package com.couchbase.lite.kmp

/**
 * **ENTERPRISE EDITION API**
 *
 * A delegate used by the replicator to create MessageEndpointConnection objects.
 *
 * Creates an object of type MessageEndpointConnection interface.
 * An application implements the MessageEndpointConnection interface using a
 * custom transportation method such as using the NearbyConnection API
 * to exchange replication data with the endpoint.
 *
 * @param endpoint the endpoint object
 * @return the MessageEndpointConnection object
 */
public typealias MessageEndpointDelegate = (endpoint: MessageEndpoint) -> MessageEndpointConnection
