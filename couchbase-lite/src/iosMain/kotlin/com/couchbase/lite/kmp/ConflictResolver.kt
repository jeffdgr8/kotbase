package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLConflict
import cocoapods.CouchbaseLite.CBLConflictResolverProtocol
import cocoapods.CouchbaseLite.CBLDocument
import platform.darwin.NSObject

internal fun ConflictResolver.convert(): CBLConflictResolverProtocol =
    DelegatedConflictResolver(this)

internal class DelegatedConflictResolver
internal constructor(internal val actual: ConflictResolver) :
    NSObject(), CBLConflictResolverProtocol {

    override fun resolve(conflict: CBLConflict): CBLDocument =
        actual(Conflict(conflict)).actual
}
