package com.couchbase.lite.kmp

import kotlinx.cinterop.*
import libcblite.CBLReplicatorStatus
import kotlin.native.internal.createCleaner

public actual class ReplicatorStatus {

    private val arena = Arena()

    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(arena) {
        it.clear()
    }

    internal val actual: CPointer<CBLReplicatorStatus>

    internal constructor(actual: CValue<CBLReplicatorStatus>) {
        this.actual = actual.getPointer(arena)
    }

    internal constructor(actual: CPointer<CBLReplicatorStatus>) {
        this.actual = actual
    }

    public actual val activityLevel: ReplicatorActivityLevel
        get() = ReplicatorActivityLevel.from(actual.pointed.activity)

    public actual val progress: ReplicatorProgress
        get() = ReplicatorProgress(actual.pointed.progress)

    public actual val error: CouchbaseLiteException?
        get() = actual.pointed.error.toException()
}
