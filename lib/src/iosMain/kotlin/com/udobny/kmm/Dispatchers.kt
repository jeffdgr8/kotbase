package com.udobny.kmm

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newFixedThreadPoolContext

private val DefaultIoScheduler = newFixedThreadPoolContext(64, "DefaultIoScheduler")

public actual val Dispatchers.IO: CoroutineDispatcher
    get() = DefaultIoScheduler
