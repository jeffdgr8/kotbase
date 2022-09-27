package com.couchbase.lite.kmp

import cnames.structs.CBLResultSet
import com.udobny.kmp.AutoCloseable
import kotlinx.cinterop.CPointer
import libcblite.*
import kotlin.native.internal.createCleaner

public actual class ResultSet
internal constructor(private val actual: CPointer<CBLResultSet>) : Iterable<Result>, AutoCloseable {

    private val memory = object {
        var closeCalled = false
        val actual = this@ResultSet.actual
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        if (!it.closeCalled) {
            CBLResultSet_Release(it.actual)
        }
    }

    public actual operator fun next(): Result? {
        CBLResultSet_Next(actual)
        return Result(actual)
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

    override fun close() {
        memory.closeCalled = true
        CBLResultSet_Release(actual)
    }
}

internal fun CPointer<CBLResultSet>.asResultSet() = ResultSet(this)
