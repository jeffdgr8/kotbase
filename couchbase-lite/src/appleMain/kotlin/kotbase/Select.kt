package kotbase

import cocoapods.CouchbaseLite.CBLQuery
import kotbase.base.AbstractDelegatedClass

public actual class Select
internal constructor(private val state: QueryState) : AbstractDelegatedClass<CBLQuery>(), Query by state {

    public actual fun from(dataSource: DataSource): From {
        return From(state.copy(from = dataSource.actual))
    }

    override val actual: CBLQuery
        get() = state.actual
}
