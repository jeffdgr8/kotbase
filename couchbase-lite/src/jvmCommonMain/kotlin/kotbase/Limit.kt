package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.Limit as CBLLimit

public actual class Limit
internal constructor(actual: CBLLimit) : DelegatedClass<CBLLimit>(actual), Query by DelegatedQuery(actual)
