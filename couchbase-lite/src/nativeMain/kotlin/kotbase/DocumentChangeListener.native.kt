package kotbase

import kotlinx.coroutines.CoroutineScope

internal sealed class DocumentChangeListenerHolder(
    val database: Database
)

internal class DocumentChangeDefaultListenerHolder(
    val listener: DocumentChangeListener,
    database: Database
) : DocumentChangeListenerHolder(database)

internal class DocumentChangeSuspendListenerHolder(
    val listener: DocumentChangeSuspendListener,
    database: Database,
    val scope: CoroutineScope
) : DocumentChangeListenerHolder(database)
