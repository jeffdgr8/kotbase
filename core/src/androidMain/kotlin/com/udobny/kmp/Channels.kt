package com.udobny.kmp

import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.trySendBlocking

@Suppress("NOTHING_TO_INLINE")
public actual inline fun <E> SendChannel<E>.trySendBlocking(element: E): ChannelResult<Unit> =
    trySendBlocking(element)
