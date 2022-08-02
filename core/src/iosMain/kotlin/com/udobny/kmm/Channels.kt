package com.udobny.kmm

import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.trySendBlocking

public actual inline fun <E> SendChannel<E>.trySendBlocking(element: E): ChannelResult<Unit> =
    trySendBlocking(element)
