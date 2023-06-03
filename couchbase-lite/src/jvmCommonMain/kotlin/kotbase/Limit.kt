package kotbase

import com.couchbase.lite.Limit
import kotbase.base.DelegatedClass

public actual class Limit
internal constructor(actual: com.couchbase.lite.Limit) :
    DelegatedClass<Limit>(actual),
    Query by DelegatedQuery(actual)
