package kotbase

import kotlinx.coroutines.CoroutineScope

internal sealed class DatabaseChangeListenerHolder(
    val database: Database
)

internal class DatabaseChangeDefaultListenerHolder(
    val listener: DatabaseChangeListener,
    database: Database
) : DatabaseChangeListenerHolder(database)

internal class DatabaseChangeSuspendListenerHolder(
    val listener: DatabaseChangeSuspendListener,
    database: Database,
    val scope: CoroutineScope
) : DatabaseChangeListenerHolder(database)
