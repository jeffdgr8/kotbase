package kotbase

import cocoapods.CouchbaseLite.CBLReplicatorProgress
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents

public actual class ReplicatorProgress
internal constructor(public val actual: CValue<CBLReplicatorProgress>) {

    public actual val completed: Long by lazy {
        actual.useContents { completed.toLong() }
    }

    public actual val total: Long by lazy {
        actual.useContents { total.toLong() }
    }
}
