@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.udobny.kmp

import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.Channel

/**
 * Adds [element] to this channel, **blocking** the caller while this channel is full,
 * and returning either [successful][ChannelResult.isSuccess] result when the element was added, or
 * failed result representing closed channel with a corresponding exception.
 *
 * This is a way to call [Channel.send] method in a safe manner inside a blocking code using [runBlocking] and catching,
 * so this function should not be used from coroutine.
 *
 * Example of usage:
 *
 * ```
 * // From callback API
 * channel.trySendBlocking(element)
 *     .onSuccess { /* request next element or debug log */ }
 *     .onFailure { t: Throwable? -> /* throw or log */ }
 * ```
 *
 * For this operation it is guaranteed that [failure][ChannelResult.failed] always contains an exception in it.
 *
 * @throws `InterruptedException` on JVM if the current thread is interrupted during the blocking send operation.
 */
public expect inline fun <E> SendChannel<E>.trySendBlocking(element: E): ChannelResult<Unit>
