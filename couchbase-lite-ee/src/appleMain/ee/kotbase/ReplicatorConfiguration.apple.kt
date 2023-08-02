package kotbase

import cocoapods.CouchbaseLite.acceptOnlySelfSignedServerCertificate

public actual var ReplicatorConfiguration.isAcceptOnlySelfSignedServerCertificate: Boolean
    get() = actual.acceptOnlySelfSignedServerCertificate
    set(value) {
        actual.acceptOnlySelfSignedServerCertificate = value
    }
