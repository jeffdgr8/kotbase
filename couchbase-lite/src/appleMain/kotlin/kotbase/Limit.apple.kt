package kotbase

import cocoapods.CouchbaseLite.CBLQuery
import kotbase.base.AbstractDelegatedClass

public actual class Limit
internal constructor(private val state: QueryState) : AbstractDelegatedClass<CBLQuery>(), Query by state {

    override val actual: CBLQuery
        get() = state.actual
}
