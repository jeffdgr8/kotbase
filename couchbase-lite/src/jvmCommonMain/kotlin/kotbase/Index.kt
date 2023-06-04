package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.Index as CBLIndex

public actual abstract class Index(actual: CBLIndex) : DelegatedClass<CBLIndex>(actual)
