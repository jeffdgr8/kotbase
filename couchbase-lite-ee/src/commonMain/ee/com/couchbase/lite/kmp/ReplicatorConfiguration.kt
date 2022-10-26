@file:JvmName("ReplicatorConfigurationEE") // https://youtrack.jetbrains.com/issue/KT-21186

package com.couchbase.lite.kmp

import kotlin.jvm.JvmName

/**
 * **ENTERPRISE EDITION API**
 *
 * Specify whether the replicator will accept any and only self-signed certificates.
 * Any non-self-signed certificates will be rejected to avoid accidentally using
 * this mode with the non-self-signed certs in production. The default value is false.
 *
 * @param acceptOnlySelfSignedServerCertificate Whether the replicator will accept
 * any and only self-signed certificates.
 * @return this.
 */
public fun ReplicatorConfiguration.setAcceptOnlySelfSignedServerCertificate(
    acceptOnlySelfSignedServerCertificate: Boolean
): ReplicatorConfiguration {
    isAcceptOnlySelfSignedServerCertificate = acceptOnlySelfSignedServerCertificate
    return this
}

/**
 * **ENTERPRISE EDITION API**
 *
 * Whether the replicator will accept any and only self-signed server certificates.
 */
public expect var ReplicatorConfiguration.isAcceptOnlySelfSignedServerCertificate: Boolean

// TODO: provide update EE KTX creator function
