@file:JvmName("ReplicatorConfigurationEEJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase

public actual var ReplicatorConfiguration.isAcceptOnlySelfSignedServerCertificate: Boolean
    get() = actual.isAcceptOnlySelfSignedServerCertificate
    set(value) {
        actual.isAcceptOnlySelfSignedServerCertificate = value
    }
