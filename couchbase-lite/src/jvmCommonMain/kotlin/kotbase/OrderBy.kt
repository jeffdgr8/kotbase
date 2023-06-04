package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.OrderBy as CBLOrderBy

public actual class OrderBy
internal constructor(actual: CBLOrderBy) : DelegatedClass<CBLOrderBy>(actual), Query by DelegatedQuery(actual) {

    public actual fun limit(limit: Expression): Limit =
        Limit(actual.limit(limit.actual))

    public actual fun limit(limit: Expression, offset: Expression?): Limit =
        Limit(actual.limit(limit.actual, offset?.actual))
}
