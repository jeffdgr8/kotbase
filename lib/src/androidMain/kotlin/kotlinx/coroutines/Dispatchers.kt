@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")

package kotlinx.coroutines

public actual val Dispatchers.IO: CoroutineDispatcher
    get() = Dispatchers.IO
