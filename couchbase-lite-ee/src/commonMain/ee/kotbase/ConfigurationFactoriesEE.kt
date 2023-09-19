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

/**
 * Create a DatabaseConfiguration, overriding the receiver's
 * values with the passed parameters:
 *
 * @param databasePath The directory in which the database is stored.
 * @param encryptionKey ENTERPRISE EDITION API: The database encryption key.
 *
 * @see DatabaseConfiguration
 */
public fun DatabaseConfiguration?.newConfig(
    databasePath: String? = null,
    encryptionKey: EncryptionKey? = null
): DatabaseConfiguration {
    return newConfig(
        databasePath = databasePath
    ).apply {
        encryptionKey?.let { setEncryptionKey(it) }
    }
}

/**
 * Create a ReplicatorConfiguration, overriding the receiver's
 * values with the passed parameters:
 *
 * Note: A document that is blocked by a document Id filter will not be auto-purged
 *       regardless of the setting of the enableAutoPurge property
 *
 * Warning: This factory method configures only the default collection!
 *          Using it on a configuration that describes any collections other than the default
 *          will lose all information associated with those collections
 *
 * @param database the local database
 * @param target (required) The replication endpoint.
 * @param type replicator type: push, pull, or push and pull: default is push and pull.
 * @param continuous continuous flag: true for continuous. False by default.
 * @param authenticator connection authenticator.
 * @param headers extra HTTP headers to send in all requests to the remote target.
 * @param pinnedServerCertificate target server's SSL certificate.
 * @param channels Sync Gateway channel names.
 * @param documentIDs IDs of documents to be replicated: default is all documents.
 * @param pushFilter filter for pushed documents.
 * @param pullFilter filter for pulled documents.
 * @param conflictResolver conflict resolver.
 * @param maxAttempts max retry attempts after connection failure.
 * @param maxAttemptWaitTime max time between retry attempts (exponential backoff).
 * @param heartbeat heartbeat interval, in seconds.
 * @param enableAutoPurge auto-purge enabled.
 * @param acceptOnlySelfSignedServerCertificate ENTERPRISE EDITION API: Whether the replicator will accept all/only self-signed certificates.
 * @param acceptParentDomainCookies Advanced: accept cookies for parent domains.
 *
 * @see ReplicatorConfiguration
 */
public fun ReplicatorConfiguration?.newConfig(
    database: Database? = null,
    target: Endpoint? = null,
    type: ReplicatorType? = null,
    continuous: Boolean? = null,
    authenticator: Authenticator? = null,
    headers: Map<String, String>? = null,
    pinnedServerCertificate: ByteArray? = null,
    channels: List<String>? = null,
    documentIDs: List<String>? = null,
    pushFilter: ReplicationFilter? = null,
    pullFilter: ReplicationFilter? = null,
    conflictResolver: ConflictResolver? = null,
    maxAttempts: Int? = null,
    maxAttemptWaitTime: Int? = null,
    heartbeat: Int? = null,
    enableAutoPurge: Boolean? = null,
    acceptOnlySelfSignedServerCertificate: Boolean? = null,
    acceptParentDomainCookies: Boolean? = null
): ReplicatorConfiguration {
    val orig = this
    return newConfig(
        database = database,
        target = target,
        type = type,
        continuous = continuous,
        authenticator = authenticator,
        headers = headers,
        pinnedServerCertificate = pinnedServerCertificate,
        channels = channels,
        documentIDs = documentIDs,
        pushFilter = pushFilter,
        pullFilter = pullFilter,
        conflictResolver = conflictResolver,
        maxAttempts = maxAttempts,
        maxAttemptWaitTime = maxAttemptWaitTime,
        heartbeat = heartbeat,
        enableAutoPurge = enableAutoPurge,
        acceptParentDomainCookies = acceptParentDomainCookies
    ).apply {
        (acceptOnlySelfSignedServerCertificate ?: orig?.isAcceptOnlySelfSignedServerCertificate)?.let {
            this.isAcceptOnlySelfSignedServerCertificate = it
        }
    }
}

/**
 * **ENTERPRISE EDITION API**
 *
 * Configuration factory for new MessageEndpointListenerConfigurations
 *
 * Usage:
 *
 *     val endpointListenerConfig = MessageEndpointListenerConfigurationFactory.newConfig(...)
 */
public val MessageEndpointListenerConfigurationFactory: MessageEndpointListenerConfiguration? = null

/**
 * **ENTERPRISE EDITION API**
 *
 * Create a MessageEndpointListenerConfiguration, overriding the receiver's
 * values with the passed parameters:
 *
 * @param database the local database.
 * @param protocolType (required) data transport type: messages or bytes.
 *
 * @see MessageEndpointListenerConfiguration
 */
public fun MessageEndpointListenerConfiguration?.newConfig(
    database: Database? = null,
    protocolType: ProtocolType? = null
): MessageEndpointListenerConfiguration = MessageEndpointListenerConfiguration(
    database ?: this?.database ?: error("Must specify a database"),
    protocolType ?: this?.protocolType ?: error("Must specify a protocol"),
)

/**
 * **ENTERPRISE EDITION API**
 *
 * Configuration factory for new URLEndpointListenerConfigurations
 *
 * Usage:
 *
 *     val endpointListenerConfig = URLEndpointListenerConfigurationFactory.newConfig(...)
 */
public val URLEndpointListenerConfigurationFactory: URLEndpointListenerConfiguration? = null

/**
 * **ENTERPRISE EDITION API**
 *
 * Create a URLEndpointListenerConfigurations, overriding the receiver's
 * values with the passed parameters:
 *
 * @param database the local database.
 * @param networkInterface the interface on which to listen: default is 0.0.0.0.
 * @param port listener port: default is next available port.
 * @param disableTls true to disable TLS: default is false.
 * @param identity certs and keys for the listener.
 * @param authenticator authenticator.
 * @param readOnly true for a read-only connection.
 * @param enableDeltaSync true to enable delta sync.
 *
 * @see URLEndpointListenerConfiguration
 */
public fun URLEndpointListenerConfiguration?.newConfig(
    database: Database? = null,
    networkInterface: String? = null,
    port: Int? = null,
    disableTls: Boolean? = null,
    identity: TLSIdentity? = null,
    authenticator: ListenerAuthenticator? = null,
    readOnly: Boolean? = null,
    enableDeltaSync: Boolean? = null,
): URLEndpointListenerConfiguration = URLEndpointListenerConfiguration(
    database ?: this?.database ?: error("Must specify a database"),
    networkInterface ?: this?.networkInterface,
    port ?: this?.port ?: error("Must specify a port"),
    disableTls ?: this?.isTlsDisabled ?: false,
    identity ?: this?.tlsIdentity,
    authenticator ?: this?.authenticator,
    readOnly ?: this?.isReadOnly ?: false,
    enableDeltaSync ?: this?.isDeltaSyncEnabled ?: false
)
