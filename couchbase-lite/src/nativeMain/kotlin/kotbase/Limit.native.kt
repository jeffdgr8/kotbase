package kotbase

public actual class Limit
internal constructor(private val state: QueryState) : Query by state