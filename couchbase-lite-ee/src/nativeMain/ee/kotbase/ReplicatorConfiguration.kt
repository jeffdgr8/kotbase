package kotbase

public actual var ReplicatorConfiguration.isAcceptOnlySelfSignedServerCertificate: Boolean
    get() = false
    set(value) {
        if (value) {
            throw UnsupportedOperationException("acceptOnlySelfSignedServerCertificate is not supported in CBL C SDK")
        }
    }
