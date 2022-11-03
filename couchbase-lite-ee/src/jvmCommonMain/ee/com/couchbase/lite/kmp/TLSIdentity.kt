package com.couchbase.lite.kmp

internal expect val TLSIdentity.actual: com.couchbase.lite.TLSIdentity

internal expect fun com.couchbase.lite.TLSIdentity.asTLSIdentity(): TLSIdentity
