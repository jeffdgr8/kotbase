package kotbase

public actual class Select
internal constructor(private val state: QueryState) : Query by state {

    public actual fun from(dataSource: DataSource): From {
        return From(state.copy(from = dataSource))
    }
}
