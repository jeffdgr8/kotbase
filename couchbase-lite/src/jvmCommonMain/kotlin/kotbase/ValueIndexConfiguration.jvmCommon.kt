package kotbase

import com.couchbase.lite.ValueIndexConfiguration as CBLValueIndexConfiguration

public actual class ValueIndexConfiguration
private constructor(actual: CBLValueIndexConfiguration) : IndexConfiguration(actual) {

    public actual constructor(vararg expressions: String) : this(CBLValueIndexConfiguration(*expressions))
}
