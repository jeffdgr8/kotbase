package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass

internal fun ConflictResolver.convert(): com.couchbase.lite.ConflictResolver =
    DelegatedConflictResolver(this)

internal class DelegatedConflictResolver
internal constructor(actual : ConflictResolver) :
    DelegatedClass<ConflictResolver>(actual),
    com.couchbase.lite.ConflictResolver {

    override fun resolve(conflict: com.couchbase.lite.Conflict): com.couchbase.lite.Document =
        actual.resolve(Conflict(conflict)).actual
}