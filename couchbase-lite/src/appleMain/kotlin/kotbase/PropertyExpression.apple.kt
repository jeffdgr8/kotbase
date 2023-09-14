package kotbase

import cocoapods.CouchbaseLite.CBLQueryExpression

public actual class PropertyExpression
internal constructor(private val property: String) : Expression(CBLQueryExpression.property(property)) {

    public actual fun from(fromAlias: String): Expression =
        DelegatedExpression(CBLQueryExpression.property(property, fromAlias))
}
