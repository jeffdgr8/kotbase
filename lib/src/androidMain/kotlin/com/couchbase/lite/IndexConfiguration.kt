@file:Suppress("ConflictingExtensionProperty")

package com.couchbase.lite

import com.couchbase.lite.kmm.Database
import com.couchbase.lite.kmm.IndexConfiguration

// TODO: consolidate as single extension when IndexConfiguration is public in 3.1
//  or remove if expressions can be made public
internal val FullTextIndexConfiguration.expressions: List<String>
    get() = expressions

internal val ValueIndexConfiguration.expressions: List<String>
    get() = expressions

@Throws(CouchbaseLiteException::class)
internal fun Database.createIndexPackageProtected(name: String, config: IndexConfiguration) {
    // TODO: casting required until actual type com.couchbase.lite.IndexConfiguration is visible in 3.1
    //  https://forums.couchbase.com/t/can-indexconfiguration-be-made-public/33772
    when (val actualConfig = config.actual) {
        is FullTextIndexConfiguration -> actual.createIndex(name, actualConfig)
        is ValueIndexConfiguration -> actual.createIndex(name, actualConfig)
    }
}
