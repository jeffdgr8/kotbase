@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")

package com.udobny.kmp

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

public actual val Dispatchers.IO: CoroutineDispatcher
    get() = Dispatchers.IO
