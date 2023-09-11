package kotbase

import cnames.structs.CBLResultSet
import kotbase.internal.DbContext
import kotlinx.cinterop.CPointer
import libcblite.CBLResultSet_Next
import libcblite.CBLResultSet_Release
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

@OptIn(ExperimentalStdlibApi::class)
public actual class ResultSet
internal constructor(
    internal val actual: CPointer<CBLResultSet>,
    private val dbContext: DbContext? = null
) : Iterable<Result>, AutoCloseable {

    private val memory = object {
        var closeCalled = false
        val actual = this@ResultSet.actual
    }

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        if (!it.closeCalled) {
            CBLResultSet_Release(it.actual)
        }
    }

    public actual operator fun next(): Result? {
        return if (CBLResultSet_Next(actual)) {
            Result(actual, dbContext)
        } else null
    }

    public actual fun allResults(): List<Result> {
        val results = mutableListOf<Result>()
        while (true) {
            val result = next() ?: break
            results.add(result)
        }
        return results
    }

    actual override fun iterator(): Iterator<Result> =
        allResults().iterator()

    actual override fun close() {
        memory.closeCalled = true
        CBLResultSet_Release(actual)
    }
}

internal fun CPointer<CBLResultSet>.asResultSet() = ResultSet(this)
