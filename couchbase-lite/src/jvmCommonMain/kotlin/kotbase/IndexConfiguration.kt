package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.IndexConfiguration as CBLIndexConfiguration

public actual open class IndexConfiguration
internal constructor(override val actual: CBLIndexConfiguration) : DelegatedClass<CBLIndexConfiguration>(actual) {

    public actual val expressions: List<String>
        get() = actual.expressions
}
