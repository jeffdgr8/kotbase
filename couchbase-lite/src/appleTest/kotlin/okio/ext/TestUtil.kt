// TODO: workaround until these extensions are merged and released in Okio
//  https://github.com/square/okio/pull/1123
@file:Suppress("INVISIBLE_MEMBER")

package okio.ext

import kotlinx.cinterop.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import okio.Buffer
import platform.Foundation.*
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal fun ByteArray.toNSData() = if (isNotEmpty()) {
    usePinned {
        NSData.create(bytes = it.addressOf(0), length = size.convert())
    }
} else {
    NSData.data()
}

fun startRunLoop(name: String = "run-loop"): NSRunLoop {
    val created = Mutex(true)
    lateinit var runLoop: NSRunLoop
    val thread = NSThread {
        runLoop = NSRunLoop.currentRunLoop
        runLoop.addPort(NSMachPort.port(), NSDefaultRunLoopMode)
        created.unlock()
        runLoop.run()
    }
    thread.name = name
    thread.start()
    runBlocking {
        created.lockWithTimeout()
    }
    return runLoop
}

suspend fun Mutex.lockWithTimeout(timeout: Duration = 5.seconds) {
    class MutexSource : Throwable()
    val source = MutexSource()
    try {
        withTimeout(timeout) { lock() }
    } catch (e: TimeoutCancellationException) {
        fail("Mutex never unlocked", source)
    }
}

fun NSStreamEvent.asString(): String {
    return when (this) {
        NSStreamEventNone -> "NSStreamEventNone"
        NSStreamEventOpenCompleted -> "NSStreamEventOpenCompleted"
        NSStreamEventHasBytesAvailable -> "NSStreamEventHasBytesAvailable"
        NSStreamEventHasSpaceAvailable -> "NSStreamEventHasSpaceAvailable"
        NSStreamEventErrorOccurred -> "NSStreamEventErrorOccurred"
        NSStreamEventEndEncountered -> "NSStreamEventEndEncountered"
        else -> "Unknown event $this"
    }
}

fun assertNoEmptySegments(buffer: Buffer) {
    assertTrue(segmentSizes(buffer).all { it != 0 }, "Expected all segments to be non-empty")
}

fun segmentSizes(buffer: Buffer): List<Int> {
    var segment = buffer.head ?: return emptyList()

    val sizes = mutableListOf(segment.limit - segment.pos)
    segment = segment.next!!
    while (segment !== buffer.head) {
        sizes.add(segment.limit - segment.pos)
        segment = segment.next!!
    }
    return sizes
}
