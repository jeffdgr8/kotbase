package kotbase

import com.couchbase.lite.ValueIndexConfiguration as CBLValueIndexConfiguration

public actual class ValueIndexConfiguration
internal constructor(override val actual: CBLValueIndexConfiguration) : IndexConfiguration(actual) {

    public actual constructor(vararg expressions: String) : this(CBLValueIndexConfiguration(*expressions))
}
