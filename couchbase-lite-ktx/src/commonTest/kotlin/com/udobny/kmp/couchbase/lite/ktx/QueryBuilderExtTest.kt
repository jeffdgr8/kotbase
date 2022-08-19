package com.udobny.kmp.couchbase.lite.ktx

import com.couchbase.lite.kmp.*
import com.couchbase.lite.kmp.Function
import com.molo17.couchbase.lite.kmp.all
import com.molo17.couchbase.lite.kmp.from
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class QueryBuilderExtTest {

    private lateinit var database: Database

    @BeforeTest
    fun setup() {
        initCouchbaseLite()
        database = createDatabase()
    }

    @AfterTest
    fun teardown() {
        database.delete()
    }

    @Test
    fun select_expression_properties() {
        val expected = QueryBuilder.select(
            SelectResult.expression(Meta.id),
            SelectResult.property("foo"),
            SelectResult.property("bar")
        ).from(DataSource.database(database))

        val actual = select(Meta.id, "foo", "bar") from database

        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_expression_results() {
        val expected = QueryBuilder.select(
            SelectResult.expression(Meta.id),
            SelectResult.all()
        ).from(DataSource.database(database))

        val actual = select(Meta.id, all()) from database

        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_count() {
        val expected = QueryBuilder.select(
            SelectResult.expression(
                Function.count(Expression.string("*"))
            )
        ).from(DataSource.database(database))

        val actual = selectCount() from database

        assertEquals(expected.explain(), actual.explain())
    }

    private fun createDatabase(): Database {
        val name = "test-db"
        val config = DatabaseConfiguration()
        return Database(name, config)
    }
}
