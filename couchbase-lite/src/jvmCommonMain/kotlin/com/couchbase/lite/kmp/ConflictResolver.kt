@file:JvmName("ConflictResolverJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package com.couchbase.lite.kmp

internal fun ConflictResolver.convert(): com.couchbase.lite.ConflictResolver {
    return com.couchbase.lite.ConflictResolver { conflict ->
        invoke(Conflict(conflict)).actual
    }
}
