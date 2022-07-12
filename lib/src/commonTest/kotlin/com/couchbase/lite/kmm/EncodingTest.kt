// TODO: internal test unecessary
//package com.couchbase.lite.kmm
//
//import com.couchbase.lite.kmm.internal.utils.Report.log
//import kotlin.math.PI
//import kotlin.test.Test
//import kotlin.test.assertEquals
//import kotlin.test.assertNotNull
//import kotlin.test.assertTrue
//
//class EncodingTest : BaseTest() {
//
//    // https://github.com/couchbase/couchbase-lite-android/issues/1453
//    @Test
//    @Throws(Exception::class)
//    fun testFLEncode() {
//        testRoundTrip(42L)
//        testRoundTrip(Long.MIN_VALUE)
//        testRoundTrip("Fleece")
//        val map = mapOf(
//            "foo" to "bar"
//        )
//        testRoundTrip(map)
//        testRoundTrip(listOf("foo" as Any, "bar"))
//        testRoundTrip(true)
//        testRoundTrip(3.14f)
//        testRoundTrip(PI)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun testFLEncodeUTF8() {
//        testRoundTrip("Goodbye cruel world") // one byte utf-8 chars
//        testRoundTrip("Goodbye cruel £ world") // a two byte utf-8 chars
//        testRoundTrip("Goodbye cruel ᘺ world") // a three byte utf-8 char
//        testRoundTrip("Hello \uD83D\uDE3A World") // a four byte utf-8 char: 😺
//        testRoundTrip("Goodbye cruel \uD83D world", "") // cheshire cat: half missing.
//        testRoundTrip("Goodbye cruel \uD83D\uC03A world", "") // a bad cat
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun testFLEncodeBadUTF8() {
//        testRoundTrip("Goodbye cruel \uD83D\uDE3A\uDE3A world", "") // a cat and a half
//    }
//
//    // Oddly windows seems to parse this differently...
//    @Test
//    @Throws(Exception::class)
//    fun testFLEncodeUTF8Win() {
//        testRoundTrip("Goodbye cruel \uD83D\uDE3A\uDE3A world") // a cat and a half
//    }
//
//    // These tests are built on the following fleece encoding.  Start at the end.
//    // 0000: 44                                [byte 0: 44: high order 4: this is a string; low order 4: 4 bytes long]
//    // 0001:     f0 9f 98 BA 00: "😺"          [bytes 1-4, cat; byte 5, 0: pad to align on even byte]
//    // 0006: 80 03             : &"😺" (@0000) [byte 0, 80: this is a pointer; byte 1, 03: 3 2-byte units ago]
//    @Test
//    fun testUTF8Slices() {
//        // https://github.com/couchbase/couchbase-lite-android/issues/1742
//        testSlice(
//            byteArrayOf(
//                0x44.toByte(),
//                0xF0.toByte(),
//                0x9F.toByte(),
//                0x98.toByte(),
//                0xBA.toByte(),
//                0x00.toByte(),
//                0x80.toByte(),
//                0x03.toByte()
//            ),
//            "\uD83D\uDE3A"
//        )
//
//        // same as above, but byte 3 of the character is not legal
//        testSlice(
//            byteArrayOf(
//                0x44.toByte(),
//                0xF0.toByte(),
//                0x9F.toByte(),
//                0x41.toByte(),
//                0xBA.toByte(),
//                0x00.toByte(),
//                0x80.toByte(),
//                0x03.toByte()
//            ),
//            null
//        )
//
//        // two 2-byte characters
//        testSlice(
//            byteArrayOf(
//                0x44.toByte(),
//                0xC2.toByte(),
//                0xA3.toByte(),
//                0xC2.toByte(),
//                0xA5.toByte(),
//                0x00.toByte(),
//                0x80.toByte(),
//                0x03.toByte()
//            ),
//            "£¥"
//        )
//
//        // two 2-byte characters, 2nd byte of 2nd char is not legal
//        testSlice(
//            byteArrayOf(
//                0x44.toByte(),
//                0xC2.toByte(),
//                0xA3.toByte(),
//                0xC2.toByte(),
//                0x41.toByte(),
//                0x00.toByte(),
//                0x80.toByte(),
//                0x03.toByte()
//            ),
//            null
//        )
//
//        // a three byte character and a one byte character
//        testSlice(
//            byteArrayOf(
//                0x44.toByte(),
//                0xE1.toByte(),
//                0x98.toByte(),
//                0xBA.toByte(),
//                0x41.toByte(),
//                0x00.toByte(),
//                0x80.toByte(),
//                0x03.toByte()
//            ),
//            "ᘺA"
//        )
//
//        // a three byte character and a continuation byte
//        testSlice(
//            byteArrayOf(
//                0x44.toByte(),
//                0xE1.toByte(),
//                0x98.toByte(),
//                0xBA.toByte(),
//                0xBA.toByte(),
//                0x00.toByte(),
//                0x80.toByte(),
//                0x03.toByte()
//            ),
//            null
//        )
//
//        // a three byte character with an illegal 2nd byte
//        testSlice(
//            byteArrayOf(
//                0x44.toByte(),
//                0xE1.toByte(),
//                0x98.toByte(),
//                0x41.toByte(),
//                0x41.toByte(),
//                0x00.toByte(),
//                0x80.toByte(),
//                0x03.toByte()
//            ),
//            null
//        )
//
//        // four single byte characters
//        testSlice(
//            byteArrayOf(
//                0x44.toByte(),
//                0x41.toByte(),
//                0x41.toByte(),
//                0x41.toByte(),
//                0x41.toByte(),
//                0x00.toByte(),
//                0x80.toByte(),
//                0x03.toByte()
//            ),
//            "AAAA"
//        )
//
//        // four single byte characters one is a continuation character
//        testSlice(
//            byteArrayOf(
//                0x44.toByte(),
//                0x41.toByte(),
//                0x98.toByte(),
//                0x41.toByte(),
//                0x41.toByte(),
//                0x00.toByte(),
//                0x80.toByte(),
//                0x03.toByte()
//            ),
//            null
//        )
//
//        // four single byte characters, byte 4 is illegal anywhere
//        testSlice(
//            byteArrayOf(
//                0x44.toByte(),
//                0x41.toByte(),
//                0x41.toByte(),
//                0x41.toByte(),
//                0xC0.toByte(),
//                0x00.toByte(),
//                0x80.toByte(),
//                0x03.toByte()
//            ),
//            null
//        )
//    }
//
//    @Throws(LiteCoreException::class)
//    private fun testRoundTrip(item: Any, expected: Any = item) {
//        FLEncoder.getManagedEncoder().use { encoder ->
//            assertTrue(encoder.writeValue(item))
//            encoder.finish2().use { slice ->
//                assertNotNull(slice)
//                val flValue: FLValue = FLValue.fromData(slice)
//                assertNotNull(flValue)
//                val obj: Any = FLValue.toObject(flValue)
//                log("ROUND TRIP SLICE: '$obj'; FROM: '$item'; EXPECTING: '$expected'")
//                assertEquals(expected, obj)
//            }
//        }
//    }
//
//    private fun testSlice(utf8Slice: ByteArray, expected: String?) {
//        val flValue: FLValue = FLValue.fromData(utf8Slice)
//        val obj: Any = FLValue.toObject(flValue)
//        log("DECODE SLICE: '$obj'; EXPECTED: '$expected'")
//        assertEquals(expected, obj)
//    }
//}
