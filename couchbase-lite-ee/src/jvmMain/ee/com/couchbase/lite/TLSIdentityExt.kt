package com.couchbase.lite

import java.security.KeyStore

internal fun deleteTLSIdentity(keyStore: KeyStore, alias: String) {
    @Suppress("VisibleForTests")
    TLSIdentity.deleteIdentity(keyStore, alias)
}
