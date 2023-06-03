package kotbase

import com.couchbase.lite.IndexConfiguration
import kotbase.base.DelegatedClass

public actual open class IndexConfiguration
internal constructor(override val actual: com.couchbase.lite.IndexConfiguration) :
    DelegatedClass<IndexConfiguration>(actual) {

    public actual val expressions: List<String>
        get() = actual.expressions
}
