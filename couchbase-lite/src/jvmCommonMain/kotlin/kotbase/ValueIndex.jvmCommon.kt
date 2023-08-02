package kotbase

import com.couchbase.lite.ValueIndex as CBLValueIndex

public actual class ValueIndex
internal constructor(override val actual: CBLValueIndex) : Index(actual)
