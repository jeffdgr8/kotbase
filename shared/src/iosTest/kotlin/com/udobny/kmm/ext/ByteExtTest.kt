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

    @Test
    fun scratch() {
        // this works
        val byteArray = ByteArray(3)
        byteArray.usePinned {
            val cPointer = it.addressOf(0).reinterpret<UInt8Var>()
            cPointer[0] = 0x1.toUByte()
            cPointer[1] = 0x2.toUByte()
            cPointer[2] = 0x3.toUByte()
        }
        assertEquals(0x1, byteArray[0])
        assertEquals(0x2, byteArray[1])
        assertEquals(0x3, byteArray[2])

        // this doesn't work
//        memScoped {
//            val cPointer = allocArray<UInt8Var>(3)
//            val byteArray2 = cPointer.readBytes(3)
//            byteArray2[0] = 0x1
//            byteArray2[1] = 0x2
//            byteArray2[2] = 0x3
//
//            assertEquals(0x1.toUByte(), cPointer[0])
//            assertEquals(0x2.toUByte(), cPointer[1])
//            assertEquals(0x3.toUByte(), cPointer[2])
//        }
    }
}
