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

import com.couchbase.lite.compareAge

/**
 * Configuration for a Replicator
 */
public expect class ReplicatorConfiguration {

    /**
     * Creates a Replicator Configuration with a set of collection configurations and
     * the target endpoint.
     *
     * @param collections the collections with configurations to replicate
     * @param target the target endpoint
     */
    public constructor(collections: kotlin.collections.Collection<CollectionConfiguration>, target: Endpoint)

    /**
     * Create a Replicator Configuration
     *
     * @param config the config to copy
     */
    public constructor(config: ReplicatorConfiguration)

    /**
     * Sets the replicator type indicating the direction of the replicator.
     * The default is ReplicatorType.PUSH_AND_PULL: bi-directional replication.
     *
     * @param type The replicator type.
     * @return this.
     */
    public fun setType(type: ReplicatorType): ReplicatorConfiguration

    /**
     * Sets whether the replicator stays active indefinitely to replicate changed documents.
     * The default is false: the replicator will stop after it finishes replicating changed documents.
     *
     * @param continuous The continuous flag.
     * @return this.
     */
    public fun setContinuous(continuous: Boolean): ReplicatorConfiguration

    /**
     * Enable/disable auto-purge.
     * The default is auto-purge enabled.
     *
     * Note: A document that is blocked by a document Id filter will not be auto-purged
     * regardless of the setting of the auto purge property
     */
    public fun setAutoPurgeEnabled(enabled: Boolean): ReplicatorConfiguration

    /**
     * Sets the extra HTTP headers to send in all requests to the remote target.
     * The default is no extra headers.
     *
     * @param headers The HTTP Headers.
     * @return this.
     */
    public fun setHeaders(headers: Map<String, String>?): ReplicatorConfiguration

    /**
     * The option to remove a restriction that does not allow a replicator to accept cookies
     * from a remote host unless the cookie domain exactly matches the domain of the sender.
     * For instance, when the option is set to false (the default), and the remote host, “bar.foo.com”,
     * sends a cookie for the domain “.foo.com”, the replicator will reject it. If the option
     * is set true, however, the replicator will accept it. This is, in general, dangerous:
     * a host might, for instance, set a cookie for the domain ".com". It is safe only when
     * the replicator is connecting only to known hosts.
     * The default value of this option is false: parent-domain cookies are not accepted
     */
    public fun setAcceptParentDomainCookies(acceptParentCookies: Boolean): ReplicatorConfiguration

    /**
     * Sets the authenticator to authenticate with a remote target server.
     * Currently, there are two types of the authenticators,
     * BasicAuthenticator and SessionAuthenticator, supported.
     * The default is no authenticator.
     *
     * @param authenticator The authenticator.
     * @return this.
     */
    public fun setAuthenticator(authenticator: Authenticator?): ReplicatorConfiguration

    /**
     * Sets the certificate used to authenticate the target server.
     * A server will be authenticated if it presents a chain of certificates (possibly of length 1)
     * in which any one of the certificates matches the one passed here.
     * The default is no pinned certificate.
     *
     * @param pinnedCert the SSL certificate.
     * @return this.
     */
    public fun setPinnedServerCertificate(pinnedCert: ByteArray?): ReplicatorConfiguration

    /**
     * Set the max number of retry attempts made after a connection failure.
     * Set to 1 for no retries and to 0 to restore default behavior.
     * The default is 10 total connection attempts (the initial attempt and up to 9 retries) for
     * a one-shot replicator and a very, very large number of retries, for a continuous replicator.
     *
     * @param maxAttempts max retry attempts
     */
    public fun setMaxAttempts(maxAttempts: Int): ReplicatorConfiguration

    /**
     * Set the max time between retry attempts, in seconds.
     * Time between retries is initially small but backs off exponentially up to this limit.
     * Once the limit is reached the interval between subsequent attempts will be
     * the value set here, until max-attempts attempts have been made.
     * The minimum value legal value is 1 second.
     * The default is 5 minutes (300 seconds). Setting the parameter to 0 will restore the default.
     *
     * @param maxAttemptWaitTime max attempt wait time
     */
    public fun setMaxAttemptWaitTime(maxAttemptWaitTime: Int): ReplicatorConfiguration

    /**
     * Set the heartbeat interval, in seconds.
     * The default is 5 minutes (300 seconds). Setting the parameter to 0 will restore the default.
     *
     * Must be non-negative and less than Integer.MAX_VALUE milliseconds
     */
    public fun setHeartbeat(heartbeat: Int): ReplicatorConfiguration

    /**
     * The replication target to replicate with.
     */
    public val target: Endpoint

    /**
     * A copy of the collection configurations associated with this replicator configuration.
     */
    public val collections: Set<CollectionConfiguration>

    /**
     * Replicator type indicating the direction of the replicator.
     */
    public var type: ReplicatorType

    /**
     * The continuous flag indicating whether the replicator should stay
     * active indefinitely to replicate changed documents.
     */
    public var isContinuous: Boolean

    /**
     * Enable/disable auto-purge.
     * Default is enabled.
     *
     * Note: A document that is blocked by a document Id filter will not be auto-purged
     * regardless of the setting of the auto purge property
     */
    public var isAutoPurgeEnabled: Boolean

    /**
     * Return Extra HTTP headers to send in all requests to the remote target.
     */
    public var headers: Map<String, String>?

    /**
     * The option to remove a restriction that does not allow a replicator to accept cookies
     * from a remote host unless the cookie domain exactly matches the domain of the sender.
     * For instance, when the option is set to false (the default), and the remote host, “bar.foo.com”,
     * sends a cookie for the domain “.foo.com”, the replicator will reject it. If the option
     * is set true, however, the replicator will accept it. This is, in general, dangerous:
     * a host might, for instance, set a cookie for the domain ".com". It is safe only when
     * the replicator is connecting only to known hosts.
     * The default value of this option is false: parent-domain cookies are not accepted
     */
    public var isAcceptParentDomainCookies: Boolean

    /**
     * The Authenticator used to authenticate the remote.
     */
    public var authenticator: Authenticator?

    /**
     * The remote target's SSL certificate.
     */
    public var pinnedServerCertificate: ByteArray?

    /**
     * The max number of retry attempts made after connection failure.
     * This method will return 0 when implicitly using the default:
     * 10 total connection attempts (the initial attempt and up to 9 retries) for
     * a one-shot replicator and a very, very large number of retries, for a continuous replicator.
     */
    public var maxAttempts: Int

    /**
     * The max time between retry attempts (exponential backoff).
     */
    public var maxAttemptWaitTime: Int

    /**
     * The heartbeat interval, in seconds.
     */
    public var heartbeat: Int

    public companion object
}

/**
 * This is a long time: just under 25 days.
 * This many seconds, however, is just less than Integer.MAX_INT millis and will fit in the heartbeat property.
 */
public val ReplicatorConfiguration.Companion.DISABLE_HEARTBEAT: Int
    get() = 2147483

/**
 * The default conflict resolution strategy.
 * Deletion always wins. A newer doc always beats an older one.
 * Otherwise, one of the two document is chosen randomly but deterministically.
 */
public val ReplicatorConfiguration.Companion.DEFAULT_CONFLICT_RESOLVER: ConflictResolver
    get() = defaultConflictResolver

private val defaultConflictResolver: ConflictResolver by lazy {
    cr@{ conflict ->
        // deletion always wins.
        val localDoc = conflict.localDocument
        val remoteDoc = conflict.remoteDocument
        if (localDoc == null || remoteDoc == null) return@cr null

        // return the newer of the two docs.
        if (localDoc.compareAge(remoteDoc) > 0) localDoc else remoteDoc
    }
}

/**
 * Allows the replicator to continue replicating in the background on iOS.
 * The default value is false, which means that the replicator will suspend itself
 * when the replicator detects that the application is running in the background.
 *
 * If setting the value to true, please ensure that the application requests for
 * extending the background task properly.
 *
 * This property has no effect on platforms other than iOS.
 */
public expect fun ReplicatorConfiguration.setAllowReplicatingInBackground(
    allowReplicatingInBackground: Boolean
): ReplicatorConfiguration

/**
 * Allows the replicator to continue replicating in the background on iOS.
 * The default value is false, which means that the replicator will suspend itself
 * when the replicator detects that the application is running in the background.
 *
 * If setting the value to true, please ensure that the application requests for
 * extending the background task properly.
 *
 * This property has no effect on platforms other than iOS.
 */
public expect var ReplicatorConfiguration.allowReplicatingInBackground: Boolean
