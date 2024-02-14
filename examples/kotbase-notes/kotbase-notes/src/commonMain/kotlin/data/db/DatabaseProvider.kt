package data.db

import kotbase.Database
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class DatabaseProvider(
    @OptIn(ExperimentalCoroutinesApi::class)
    val writeScope: CoroutineScope = CoroutineScope(
        CoroutineName("db-write") + Dispatchers.IO.limitedParallelism(1)
    ),
    val readContext: CoroutineContext = CoroutineName("db-read") + Dispatchers.IO
) {

    val database by lazy { Database(DB_NAME) }

    companion object {
        private const val DB_NAME = "kotbase-notes"
    }
}
