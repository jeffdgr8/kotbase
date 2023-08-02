package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.Select as CBLSelect

public actual class Select
internal constructor(actual: CBLSelect) : DelegatedClass<CBLSelect>(actual), Query by DelegatedQuery(actual) {

    public actual fun from(dataSource: DataSource): From =
        From(actual.from(dataSource.actual))
}
