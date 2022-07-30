package kotlinx.coroutines

private val DefaultIoScheduler = newFixedThreadPoolContext(64, "DefaultIoScheduler")

public actual val Dispatchers.IO: CoroutineDispatcher
    get() = DefaultIoScheduler
