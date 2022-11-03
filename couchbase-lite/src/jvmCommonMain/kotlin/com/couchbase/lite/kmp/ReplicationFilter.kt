@file:JvmName("ReplicationFilterJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package com.couchbase.lite.kmp

internal fun ReplicationFilter.convert(): com.couchbase.lite.ReplicationFilter {
    return com.couchbase.lite.ReplicationFilter { document, flags ->
        invoke(Document(document), flags)
    }
}
