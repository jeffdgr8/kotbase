// TODO: workaround until these extensions are merged and released in Okio
//  https://github.com/square/okio/pull/1123
@file:Suppress("INVISIBLE_MEMBER")

package okio.temp

import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import okio.Buffer
import platform.Foundation.NSData
import platform.Foundation.create
import kotlin.test.assertTrue

fun ByteArray.toNSData(): NSData = memScoped {
    NSData.create(bytes = allocArrayOf(this@toNSData), length = size.convert())
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
