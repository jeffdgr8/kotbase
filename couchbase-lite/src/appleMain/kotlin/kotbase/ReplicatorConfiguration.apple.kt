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

import cocoapods.CouchbaseLite.CBLReplicatorConfiguration
import kotbase.ext.toByteArray
import kotbase.ext.toSecCertificate
import kotbase.internal.DelegatedClass
import kotlinx.cinterop.convert
import platform.Security.SecCertificateRef

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
        // CBLReplicatorConfiguration throws an uncatchable NSInvalidArgumentException
        // for collections from different database instances: validate first
        CBLReplicatorConfiguration(collections.validated().map { it.actual }, target.actual),
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
        get() = ReplicatorType.from(actual.replicatorType)
        set(value) {
            actual.replicatorType = value.actual
        }

    public actual var isContinuous: Boolean
        get() = actual.continuous
        set(value) {
            actual.continuous = value
        }

    public actual var isAutoPurgeEnabled: Boolean
        get() = actual.enableAutoPurge
        set(value) {
            actual.enableAutoPurge = value
        }

    @Suppress("UNCHECKED_CAST")
    public actual var headers: Map<String, String>?
        get() = actual.headers as Map<String, String>?
        set(value) {
            actual.headers = value as Map<Any?, *>?
        }

    public actual var isAcceptParentDomainCookies: Boolean
        get() = actual.acceptParentDomainCookies
        set(value) {
            actual.acceptParentDomainCookies = value
        }

    public actual var authenticator: Authenticator? = authenticator
        set(value) {
            field = value
            actual.authenticator = value?.actual
        }

    public actual var pinnedServerCertificate: ByteArray?
        get() = actual.pinnedServerCertificate?.toByteArray()
        set(value) {
            actual.pinnedServerCertificate = value?.toSecCertificate()
        }

    public actual var maxAttempts: Int
        get() = actual.maxAttempts.toInt()
        set(value) {
            require(value >= 0) { "max attempts must be >=0" }
            actual.maxAttempts = value.convert()
        }

    public actual var maxAttemptWaitTime: Int
        get() = actual.maxAttemptWaitTime.toInt()
        set(value) {
            require(value >= 0) { "max attempt wait time must be >=0" }
            actual.maxAttemptWaitTime = value.toDouble()
        }

    public actual var heartbeat: Int
        get() = actual.heartbeat.toInt()
        set(value) {
            require(value >= 0) { "heartbeat must be >=0" }
            val millis = value * 1000L
            require(millis <= Int.MAX_VALUE) { "heartbeat too large" }
            actual.heartbeat = value.toDouble()
        }

    override fun toString(): String {
        return buildString {
            append("ReplicatorConfig{(")
            collectionConfigurations.keys.forEachIndexed { i, c ->
                if (i != 0) append(", ")
                append(c.fullName)
            }
            append(") ")

            when (type) {
                ReplicatorType.PULL, ReplicatorType.PUSH_AND_PULL -> append('<')
                else -> {}
            }

            append(if (isContinuous) '*' else 'o')

            when (type) {
                ReplicatorType.PUSH, ReplicatorType.PUSH_AND_PULL -> append('>')
                else -> {}
            }

            if (authenticator != null) append('@')
            if (pinnedServerCertificate != null) append('^')

            append(target).append('}')
        }
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
public fun ReplicatorConfiguration.setPinnedServerSecCertificate(
    pinnedCert: SecCertificateRef?
): ReplicatorConfiguration {
    actual.pinnedServerCertificate = pinnedCert
    return this
}

/**
 * The remote target’s SSL certificate.
 */
public var ReplicatorConfiguration.pinnedServerSecCertificate: SecCertificateRef?
    get() = actual.pinnedServerCertificate
    set(value) {
        actual.pinnedServerCertificate = value
    }

private fun kotlin.collections.Collection<CollectionConfiguration>.validated(): kotlin.collections.Collection<CollectionConfiguration> {
    val db = firstOrNull()?.collection?.database?.actual
    forEach { config ->
        require(config.collection.database.actual == db) {
            "Cannot create a configuration for collections from different databases"
        }
    }
    return this
}
