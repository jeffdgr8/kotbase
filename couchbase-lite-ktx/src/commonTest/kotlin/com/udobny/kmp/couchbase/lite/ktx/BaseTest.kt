package com.udobny.kmp.couchbase.lite.ktx

import com.couchbase.lite.kmp.Database
import com.couchbase.lite.kmp.DatabaseConfiguration
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class BaseTest {

    protected lateinit var database: Database

    @BeforeTest
    fun setup() {
        initCouchbaseLite()
        database = createDatabase()
    }

    @AfterTest
    fun teardown() {
        database.delete()
    }

    private fun createDatabase(): Database {
        val name = "test-db"
        val config = DatabaseConfiguration()
        return Database(name, config)
    }
}
