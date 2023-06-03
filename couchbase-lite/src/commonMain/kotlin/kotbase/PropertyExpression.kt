package kotbase

/**
 * Property expression
 */
public expect class PropertyExpression : Expression {

    /**
     * Specifies an alias name of the data source to query the data from.
     *
     * @param fromAlias The alias name of the data source.
     * @return The property Expression with the given data source alias name.
     */
    public fun from(fromAlias: String): Expression
}
