package kotbase

import cocoapods.CouchbaseLite.CBLConflict
import cocoapods.CouchbaseLite.CBLConflictResolverProtocol
import cocoapods.CouchbaseLite.CBLDocument
import platform.darwin.NSObject

internal fun ConflictResolver.convert(): CBLConflictResolverProtocol {
    return object : NSObject(), CBLConflictResolverProtocol {

        override fun resolve(conflict: CBLConflict): CBLDocument? =
            invoke(Conflict(conflict))?.actual
    }
}
