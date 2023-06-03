package kotbase

import com.couchbase.lite.Index
import kotbase.base.DelegatedClass

public actual abstract class Index(actual: com.couchbase.lite.Index) :
    DelegatedClass<Index>(actual)
