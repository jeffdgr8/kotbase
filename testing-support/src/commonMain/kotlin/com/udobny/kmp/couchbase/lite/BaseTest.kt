package com.udobny.kmp.couchbase.lite

import com.couchbase.lite.kmp.Database
import com.couchbase.lite.kmp.DatabaseConfiguration
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

// TODO: move BaseDbTest to testing-support module to share base test classes
abstract class BaseTest {

    init {
        initCouchbaseLite()
    }

    protected lateinit var database: Database

    @BeforeTest
    fun setup() {
        database = createDatabase()
    }

    @AfterTest
    fun teardown() {
        database.delete()
    }

    private fun createDatabase(): Database {
        val name = getUniqueName("test-db", 8)
        val config = DatabaseConfiguration()
        return Database(name, config)
    }

    private fun getUniqueName(prefix: String, len: Int): String {
        return prefix + '_' + randomString(len)
    }

    private fun randomString(len: Int): String {
        val buf = CharArray(len)
        for (idx in buf.indices) {
            buf[idx] = CHARS[Random.nextInt(CHARS.size)]
        }
        return buf.concatToString()
    }

    private companion object {
        private const val ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val NUMERIC = "0123456789"
        private val ALPHANUMERIC = NUMERIC + ALPHA + ALPHA.lowercase()
        private val CHARS = ALPHANUMERIC.toCharArray()
    }
}
