@file:JvmName("ConflictResolverJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

internal fun ConflictResolver.convert(): com.couchbase.lite.ConflictResolver =
    DelegatedConflictResolver(this)

internal class DelegatedConflictResolver
internal constructor(actual: ConflictResolver) :
    DelegatedClass<ConflictResolver>(actual),
    com.couchbase.lite.ConflictResolver {

    override fun resolve(conflict: com.couchbase.lite.Conflict): com.couchbase.lite.Document =
        actual(Conflict(conflict)).actual
}
