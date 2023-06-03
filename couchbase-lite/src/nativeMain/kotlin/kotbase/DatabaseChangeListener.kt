package kotbase

internal class DatabaseChangeListenerHolder(
    val listener: DatabaseChangeListener,
    val database: Database
)
