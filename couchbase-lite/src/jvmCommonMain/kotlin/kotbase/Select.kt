package kotbase

import com.couchbase.lite.Select
import kotbase.base.DelegatedClass

public actual class Select
internal constructor(actual: com.couchbase.lite.Select) :
    DelegatedClass<Select>(actual),
    Query by DelegatedQuery(actual) {

    public actual fun from(dataSource: DataSource): From =
        From(actual.from(dataSource.actual))
}
