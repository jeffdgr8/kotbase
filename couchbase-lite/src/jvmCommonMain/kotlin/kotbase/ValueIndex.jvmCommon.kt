package kotbase

import com.couchbase.lite.ValueIndex as CBLValueIndex

public actual class ValueIndex
internal constructor(actual: CBLValueIndex) : Index(actual)

internal val ValueIndex.actual: CBLValueIndex
    get() = platformState!!.actual as CBLValueIndex
