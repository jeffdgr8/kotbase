package com.couchbase.lite

internal fun deleteTLSIdentity(alias: String) {
    @Suppress("VisibleForTests")
    TLSIdentity.deleteIdentity(alias)
}
