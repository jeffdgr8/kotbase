package kotbase

import cocoapods.CouchbaseLite.CBLIndexConfiguration
import kotbase.base.DelegatedClass

public actual open class IndexConfiguration
internal constructor(override val actual: CBLIndexConfiguration) :
    DelegatedClass<CBLIndexConfiguration>(actual) {

    @Suppress("UNCHECKED_CAST")
    public actual val expressions: List<String>
        get() = actual.expressions as List<String>
}
