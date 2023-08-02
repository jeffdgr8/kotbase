package kotbase

import kotlinx.coroutines.CoroutineScope

internal sealed class QueryChangeListenerHolder(
    val query: Query
)

internal class QueryChangeDefaultListenerHolder(
    val listener: QueryChangeListener,
    query: Query
) : QueryChangeListenerHolder(query)

internal class QueryChangeSuspendListenerHolder(
    val listener: QueryChangeSuspendListener,
    query: Query,
    val scope: CoroutineScope
) : QueryChangeListenerHolder(query)
