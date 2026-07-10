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
import java.security.cert.X509Certificate
import com.couchbase.lite.ReplicatorConfiguration as CBLReplicatorConfiguration

public actual class ReplicatorConfiguration
private constructor(
    actual: CBLReplicatorConfiguration,
    public actual val target: Endpoint,
    private val collectionConfigurations: MutableMap<Collection, CollectionConfiguration>,
    authenticator: Authenticator? = null
) : DelegatedClass<CBLReplicatorConfiguration>(actual) {

    public actual constructor(
        collections: kotlin.collections.Collection<CollectionConfiguration>,
        target: Endpoint
    ) : this(
        CBLReplicatorConfiguration(collections.map { it.actual }, target.actual),
        target,
        collections.associateByTo(mutableMapOf()) { it.collection }
    )

    public actual constructor(config: ReplicatorConfiguration) : this(
        CBLReplicatorConfiguration(config.actual),
        config.target,
        config.collectionConfigurations.toMutableMap(),
        config.authenticator
    )

    public actual fun setType(type: ReplicatorType): ReplicatorConfiguration {
        this@ReplicatorConfiguration.type = type
        return this
    }

    public actual fun setContinuous(continuous: Boolean): ReplicatorConfiguration {
        this@ReplicatorConfiguration.isContinuous = continuous
        return this
    }

    public actual fun setAutoPurgeEnabled(enabled: Boolean): ReplicatorConfiguration {
        this@ReplicatorConfiguration.isAutoPurgeEnabled = enabled
        return this
    }

    public actual fun setHeaders(headers: Map<String, String>?): ReplicatorConfiguration {
        this@ReplicatorConfiguration.headers = headers
        return this
    }

    public actual fun setAcceptParentDomainCookies(acceptParentCookies: Boolean): ReplicatorConfiguration {
        this@ReplicatorConfiguration.isAcceptParentDomainCookies = acceptParentCookies
        return this
    }

    public actual fun setAuthenticator(authenticator: Authenticator?): ReplicatorConfiguration {
        this@ReplicatorConfiguration.authenticator = authenticator
        return this
    }

    public actual fun setPinnedServerCertificate(pinnedCert: ByteArray?): ReplicatorConfiguration {
        this@ReplicatorConfiguration.pinnedServerCertificate = pinnedCert
        return this
    }

    public actual fun setMaxAttempts(maxAttempts: Int): ReplicatorConfiguration {
        this@ReplicatorConfiguration.maxAttempts = maxAttempts
        return this
    }

    public actual fun setMaxAttemptWaitTime(maxAttemptWaitTime: Int): ReplicatorConfiguration {
        this@ReplicatorConfiguration.maxAttemptWaitTime = maxAttemptWaitTime
        return this
    }

    public actual fun setHeartbeat(heartbeat: Int): ReplicatorConfiguration {
        this@ReplicatorConfiguration.heartbeat = heartbeat
        return this
    }

    public actual val collections: Set<CollectionConfiguration>
        get() = collectionConfigurations.values.toSet()

    public actual var type: ReplicatorType
        get() = ReplicatorType.from(actual.type)
        set(value) {
            actual.type = value.actual
        }

    public actual var isContinuous: Boolean
        get() = actual.isContinuous
        set(value) {
            actual.isContinuous = value
        }

    public actual var isAutoPurgeEnabled: Boolean
        get() = actual.isAutoPurgeEnabled
        set(value) {
            actual.isAutoPurgeEnabled = value
        }

    public actual var headers: Map<String, String>?
        get() = actual.headers
        set(value) {
            actual.headers = value
        }

    public actual var isAcceptParentDomainCookies: Boolean
        get() = actual.isAcceptParentDomainCookies
        set(value) {
            actual.isAcceptParentDomainCookies = value
        }

    public actual var authenticator: Authenticator? = authenticator
        set(value) {
            field = value
            actual.setAuthenticator(value?.actual)
        }

    @Suppress("DEPRECATION")
    public actual var pinnedServerCertificate: ByteArray?
        get() = actual.pinnedServerCertificate
        set(value) {
            actual.pinnedServerCertificate = value
        }

    public actual var maxAttempts: Int
        get() = actual.maxAttempts
        set(value) {
            actual.maxAttempts = value
        }

    public actual var maxAttemptWaitTime: Int
        get() = actual.maxAttemptWaitTime
        set(value) {
            actual.maxAttemptWaitTime = value
        }

    public actual var heartbeat: Int
        get() = actual.heartbeat
        set(value) {
            actual.heartbeat = value
        }

    public actual companion object
}

/**
 * Sets the certificate used to authenticate the target server.
 * A server will be authenticated if it presents a chain of certificates (possibly of length 1)
 * in which any one of the certificates matches the one passed here.
 * The default is no pinned certificate.
 *
 * @param pinnedCert the SSL certificate.
 * @return this.
 */
public fun ReplicatorConfiguration.setPinnedServerX509Certificate(
    pinnedCert: X509Certificate?
): ReplicatorConfiguration {
    actual.setPinnedServerX509Certificate(pinnedCert)
    return this
}

/**
 * The remote target's SSL certificate.
 */
public var ReplicatorConfiguration.pinnedServerX509Certificate: X509Certificate?
    get() = actual.pinnedServerX509Certificate
    set(value) {
        actual.setPinnedServerX509Certificate(value)
    }

public actual fun ReplicatorConfiguration.setAllowReplicatingInBackground(allowReplicatingInBackground: Boolean): ReplicatorConfiguration {
    // no-op
    return this
}

public actual var ReplicatorConfiguration.allowReplicatingInBackground: Boolean
    get() = Defaults.Replicator.ALLOW_REPLICATING_IN_BACKGROUND
    set(_) {
        // no-op
    }
