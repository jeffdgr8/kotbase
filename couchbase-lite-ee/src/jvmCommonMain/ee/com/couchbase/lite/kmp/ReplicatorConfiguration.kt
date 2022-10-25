@file:JvmName("ReplicatorConfigurationJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package com.couchbase.lite.kmp

public actual var ReplicatorConfiguration.isAcceptOnlySelfSignedServerCertificate: Boolean
    get() = actual.isAcceptOnlySelfSignedServerCertificate
    set(value) {
        actual.isAcceptOnlySelfSignedServerCertificate = value
    }
