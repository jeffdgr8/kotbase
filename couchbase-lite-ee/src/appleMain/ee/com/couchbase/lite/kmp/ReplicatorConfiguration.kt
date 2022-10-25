package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.acceptOnlySelfSignedServerCertificate

public actual var ReplicatorConfiguration.isAcceptOnlySelfSignedServerCertificate: Boolean
    get() = actual.acceptOnlySelfSignedServerCertificate
    set(value) {
        actual.acceptOnlySelfSignedServerCertificate = value
    }
