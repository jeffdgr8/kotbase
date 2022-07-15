package com.couchbase.lite.kmm

import com.udobny.kmm.test.IgnoreIos
import kotlin.test.*

// iOS doesn't need initializing
@IgnoreIos
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
    @Throws(CouchbaseLiteException::class)
    fun testCreateDatabaseBeforeInit() {
        assertFailsWith<IllegalStateException> {
            Database("fail", DatabaseConfiguration())
        }
    }

    // ConsoleLogger is accessed and cached lazily before initialized state is reset, otherwise this would pass
    @Ignore
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
