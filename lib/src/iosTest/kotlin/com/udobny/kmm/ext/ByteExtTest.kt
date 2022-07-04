package com.udobny.kmm.ext

import kotlinx.cinterop.*
import okio.FileSystem
import platform.Foundation.*
import platform.darwin.UInt8Var
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ByteExtTest {

    @Test
    fun test_toNSData() {
        val input = byteArrayOf(0x1, 0x2, 0x3)
        val output = input.toNSData()
        assertEquals(3UL, output.length)
        val bytes = output.bytes!!.readBytes(3)
        assertNotEquals(input, bytes, "not the same reference")
        assertEquals(0x1, bytes[0])
        assertEquals(0x2, bytes[1])
        assertEquals(0x3, bytes[2])
    }

    @Test
    fun test_toByteArray() {
        val input = NSData.create(base64Encoding = "AQID")!!
        val output = input.toByteArray()
        assertEquals(0x1, output[0])
        assertEquals(0x2, output[1])
        assertEquals(0x3, output[2])
    }
}
