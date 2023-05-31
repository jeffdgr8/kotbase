package com.couchbase.lite.kmp

import com.couchbase.lite.internal.CouchbaseLiteInternal
import kotlin.test.*

// native doesn't need initializing
class PreInitTest : BaseTest() {

    @BeforeTest
    fun setUpPreInitTest() {
        CouchbaseLiteInternal.reset(false)
    }

    @AfterTest
    fun tearDownPreInitTest() {
        CouchbaseLiteInternal.reset(true)
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
