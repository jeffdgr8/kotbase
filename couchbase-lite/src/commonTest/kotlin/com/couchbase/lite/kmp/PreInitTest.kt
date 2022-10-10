package com.couchbase.lite.kmp

import com.udobny.kmp.test.IgnoreApple
import com.udobny.kmp.test.IgnoreNative
import kotlin.test.*

// native doesn't need initializing
@IgnoreNative
@IgnoreApple
class PreInitTest : BaseTest() {

    @BeforeTest
    fun setUpPreInitTest() {
        couchbaseLiteReset(false)
    }

    @AfterTest
    fun tearDownPreInitTest() {
        couchbaseLiteReset(true)
    }

    @Test
    fun testCreateDatabaseBeforeInit() {
        assertFailsWith<IllegalStateException> {
            Database("fail", DatabaseConfiguration())
        }
    }

    @Test
    fun testGetConsoleBeforeInit() {
        assertFailsWith<IllegalStateException> {
            Database.log.console
        }
    }

    @Test
    fun testGetFileBeforeInit() {
        assertFailsWith<IllegalStateException> {
            Database.log.file
        }
    }

    @Test
    fun testCreateDBConfigBeforeInit() {
        assertFailsWith<IllegalStateException> {
            DatabaseConfiguration()
        }
    }
}

internal expect fun couchbaseLiteReset(state: Boolean)
