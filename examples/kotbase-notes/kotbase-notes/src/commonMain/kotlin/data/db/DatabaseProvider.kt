package data.db

import kotbase.Database
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class DatabaseProvider(
    val readContext: CoroutineContext = CoroutineName("db-read") + Dispatchers.IO,
    @OptIn(ExperimentalCoroutinesApi::class)
    val writeContext: CoroutineContext = CoroutineName("db-write") + Dispatchers.IO.limitedParallelism(1),
    val scope: CoroutineScope = CoroutineScope(writeContext)
) {

    val database by lazy { Database(DB_NAME) }

    companion object {
        private const val DB_NAME = "kotbase-notes"
    }
}
