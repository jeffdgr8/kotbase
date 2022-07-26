package com.couchbase.lite.kmm

import com.couchbase.lite.asJSON
import com.couchbase.lite.kmm.internal.utils.Report
import com.couchbase.lite.kmm.internal.utils.paddedString
import com.udobny.kmm.test.IgnoreIos
import com.udobny.kmm.use
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.CountDownLatch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.*
import kotlin.math.*
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class QueryTest : BaseQueryTest() {

    private class MathFn(val name: String, val expr: Expression, val expected: Double)

    private class TestCase(val expr: Expression, vararg documentIDs: Int) {
        val docIds = documentIDs.map { "doc$it" }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testQueryGetColumnNameAfter32Items() {
        val document = MutableDocument("doc")
        document.setString("key", "value")
        saveDocInBaseTestDb(document)

        val query = """select
                `1`,`2`,`3`,`4`,`5`,`6`,`7`,`8`,`9`,`10`,`11`,`12`,
                `13`,`14`,`15`,`16`,`17`,`18`,`19`,`20`,`21`,`22`,`23`,`24`,
                `25`,`26`,`27`,`28`,`29`,`30`,`31`,`32`, `key` from _ limit 1"""

        val queryBuild = QueryBuilder.createQuery(query, baseTestDb)

        //expected results
        val key = "key"
        val value = "value"

        val arrayResult = mutableListOf<String?>()
        for (i in 0 until 32) {
            arrayResult.add(null)
        }
        arrayResult.add(value)

        val mapResult = mapOf(
            key to value
        )

        queryBuild.execute().use { rs ->
            while (true) {
                val result = rs.next() ?: break
                assertEquals("{\"key\":\"value\"}", result.toJSON())
                assertEquals(arrayResult, result.toList())
                assertEquals(mapResult, result.toMap())
                assertEquals(value, result.getValue(key).toString())
                assertEquals(value, result.getString(key))
                assertEquals(value, result.getString(32))
            }
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testQueryDocumentExpiration() = runBlocking {
        val now = Clock.System.now()

        // this one should expire
        val doc = MutableDocument("doc")
        doc.setInt("answer", 42)
        doc.setString("notHere", "string")
        saveDocInBaseTestDb(doc)
        baseTestDb.setDocumentExpiration("doc", now + 500.milliseconds)

        // this one is deleted
        val doc10 = MutableDocument("doc10")
        doc10.setInt("answer", 42)
        doc10.setString("notHere", "string")
        saveDocInBaseTestDb(doc10)
        baseTestDb.setDocumentExpiration("doc10", now + 2000.milliseconds) //deleted doc
        baseTestDb.delete(doc10)

        // should be in the result set
        val doc1 = MutableDocument("doc1")
        doc1.setInt("answer", 42)
        doc1.setString("a", "string")
        saveDocInBaseTestDb(doc1)
        baseTestDb.setDocumentExpiration("doc1", now + 2000.milliseconds)

        // should be in the result set
        val doc2 = MutableDocument("doc2")
        doc2.setInt("answer", 42)
        doc2.setString("b", "string")
        saveDocInBaseTestDb(doc2)
        baseTestDb.setDocumentExpiration("doc2", now + 3000.milliseconds)

        // should be in the result set
        val doc3 = MutableDocument("doc3")
        doc3.setInt("answer", 42)
        doc3.setString("c", "string")
        saveDocInBaseTestDb(doc3)
        baseTestDb.setDocumentExpiration("doc3", now + 4000.milliseconds)

        delay(1000)

        // This should get all but the one that has expired
        // and the one that was deleted
        val query = QueryBuilder.select(SR_DOCID, SR_EXPIRATION)
            .from(DataSource.database(baseTestDb))
            .where(Meta.expiration.lessThan(Expression.longValue(now.toEpochMilliseconds() + 6000L)))

        val rows = verifyQuery(query, false) { _, _ -> }
        assertEquals(3, rows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testQueryDocumentIsNotDeleted() {
        val doc1a = MutableDocument("doc1")
        doc1a.setInt("answer", 42)
        doc1a.setString("a", "string")
        baseTestDb.save(doc1a)

        val query = QueryBuilder.select(SR_DOCID, SR_DELETED)
            .from(DataSource.database(baseTestDb))
            .where(
                Meta.id.equalTo(Expression.string("doc1"))
                    .and(Meta.deleted.equalTo(Expression.booleanValue(false)))
            )

        val rows = verifyQuery(query, false) { _, result ->
            assertEquals(result.getString(0), "doc1")
            assertFalse(result.getBoolean(1))
        }
        assertEquals(1, rows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testQueryDocumentIsDeleted() {
        val doc = MutableDocument("doc1")
        doc.setInt("answer", 42)
        doc.setString("a", "string")
        saveDocInBaseTestDb(doc)

        baseTestDb.delete(baseTestDb.getDocument("doc1")!!)

        val query = QueryBuilder.select(SR_DOCID, SR_DELETED)
            .from(DataSource.database(baseTestDb))
            .where(
                Meta.deleted.equalTo(Expression.booleanValue(true))
                    .and(Meta.id.equalTo(Expression.string("doc1")))
            )

        assertEquals(1, verifyQuery(query, false) { _, _ -> })
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testNoWhereQuery() {
        loadJSONResource("names_100.json")
        val numRows = verifyQuery(
            QueryBuilder.select(SR_DOCID, SR_SEQUENCE).from(DataSource.database(baseTestDb))
        ) { n, result ->
            val docID = result.getString(0)
            val expectedID = "doc-${n.paddedString(3)}"
            assertEquals(expectedID, docID)

            val sequence = result.getInt(1)
            assertEquals(n, sequence)

            val doc = baseTestDb.getDocument(docID!!)
            assertEquals(expectedID, doc!!.id)
            assertEquals(n.toLong(), doc.sequence)
        }
        assertEquals(100, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testWhereComparison() {
        loadNumberedDocs(10)
        runTestCases(
            TestCase(EXPR_NUMBER1.lessThan(Expression.intValue(3)), 1, 2),
            TestCase(
                EXPR_NUMBER1.greaterThanOrEqualTo(Expression.intValue(3)),
                3, 4, 5, 6, 7, 8, 9, 10
            ),
            TestCase(EXPR_NUMBER1.lessThanOrEqualTo(Expression.intValue(3)), 1, 2, 3),
            TestCase(EXPR_NUMBER1.greaterThan(Expression.intValue(3)), 4, 5, 6, 7, 8, 9, 10),
            TestCase(EXPR_NUMBER1.greaterThan(Expression.intValue(6)), 7, 8, 9, 10),
            TestCase(EXPR_NUMBER1.lessThanOrEqualTo(Expression.intValue(6)), 1, 2, 3, 4, 5, 6),
            TestCase(EXPR_NUMBER1.greaterThanOrEqualTo(Expression.intValue(6)), 6, 7, 8, 9, 10),
            TestCase(EXPR_NUMBER1.lessThan(Expression.intValue(6)), 1, 2, 3, 4, 5),
            TestCase(EXPR_NUMBER1.equalTo(Expression.intValue(7)), 7),
            TestCase(EXPR_NUMBER1.notEqualTo(Expression.intValue(7)), 1, 2, 3, 4, 5, 6, 8, 9, 10)
        )
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testWhereArithmetic() {
        loadNumberedDocs(10)
        runTestCases(
            TestCase(
                EXPR_NUMBER1.multiply(Expression.intValue(2)).greaterThan(Expression.intValue(3)),
                2, 3, 4, 5, 6, 7, 8, 9, 10
            ),
            TestCase(
                EXPR_NUMBER1.divide(Expression.intValue(2)).greaterThan(Expression.intValue(3)),
                8, 9, 10
            ),
            TestCase(
                EXPR_NUMBER1.modulo(Expression.intValue(2)).equalTo(Expression.intValue(0)),
                2, 4, 6, 8, 10
            ),
            TestCase(
                EXPR_NUMBER1.add(Expression.intValue(5)).greaterThan(Expression.intValue(10)),
                6, 7, 8, 9, 10
            ),
            TestCase(
                EXPR_NUMBER1.subtract(Expression.intValue(5)).greaterThan(Expression.intValue(0)),
                6, 7, 8, 9, 10
            ),
            TestCase(
                EXPR_NUMBER1.multiply(EXPR_NUMBER2).greaterThan(Expression.intValue(10)),
                2, 3, 4, 5, 6, 7, 8
            ),
            TestCase(
                EXPR_NUMBER2.divide(EXPR_NUMBER1).greaterThan(Expression.intValue(3)),
                1, 2
            ),
            TestCase(
                EXPR_NUMBER2.modulo(EXPR_NUMBER1).equalTo(Expression.intValue(0)),
                1, 2, 5, 10
            ),
            TestCase(
                EXPR_NUMBER1.add(EXPR_NUMBER2).equalTo(Expression.intValue(10)),
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10
            ),
            TestCase(
                EXPR_NUMBER1.subtract(EXPR_NUMBER2).greaterThan(Expression.intValue(0)),
                6, 7, 8, 9, 10
            )
        )
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testWhereAndOr() {
        loadNumberedDocs(10)
        runTestCases(
            TestCase(
                EXPR_NUMBER1.greaterThan(Expression.intValue(3))
                    .and(EXPR_NUMBER2.greaterThan(Expression.intValue(3))),
                4, 5, 6
            ),
            TestCase(
                EXPR_NUMBER1.lessThan(Expression.intValue(3))
                    .or(EXPR_NUMBER2.lessThan(Expression.intValue(3))),
                1, 2, 8, 9, 10
            )
        )
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testWhereValued() {
        val doc1 = MutableDocument("doc1")
        doc1.setValue("name", "Scott")
        doc1.setValue("address", null)
        saveDocInBaseTestDb(doc1)

        val doc2 = MutableDocument("doc2")
        doc2.setValue("name", "Tiger")
        doc2.setValue("address", "123 1st ave.")
        doc2.setValue("age", 20)
        saveDocInBaseTestDb(doc2)

        val name = Expression.property("name")
        val address = Expression.property("address")
        val age = Expression.property("age")
        val work = Expression.property("work")

        val cases = arrayOf(
            TestCase(name.isNotValued()),
            TestCase(name.isValued(), 1, 2),
            TestCase(address.isNotValued(), 1),
            TestCase(address.isValued(), 2),
            TestCase(age.isNotValued(), 1),
            TestCase(age.isValued(), 2),
            TestCase(work.isNotValued(), 1, 2),
            TestCase(work.isValued())
        )

        for (testCase in cases) {
            val numRows = verifyQuery(
                QueryBuilder.select(SR_DOCID).from(DataSource.database(baseTestDb))
                    .where(testCase.expr)
            ) { n, result ->
                if (n <= testCase.docIds.size) {
                    assertEquals(testCase.docIds[n - 1], result.getString(0))
                }
            }
            assertEquals(testCase.docIds.size, numRows)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testWhereIs() {
        val doc1 = MutableDocument()
        doc1.setValue("string", "string")
        saveDocInBaseTestDb(doc1)

        val query = QueryBuilder.select(SR_DOCID)
            .from(DataSource.database(baseTestDb))
            .where(Expression.property("string").`is`(Expression.string("string")))

        val numRows = verifyQuery(query) { _, result ->
            val docID = result.getString(0)!!
            assertEquals(doc1.id, docID)
            val doc = baseTestDb.getDocument(docID)!!
            assertEquals(doc1.getValue("string"), doc.getValue("string"))
        }
        assertEquals(1, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testWhereIsNot() {
        val doc1 = MutableDocument()
        doc1.setValue("string", "string")
        saveDocInBaseTestDb(doc1)

        val query = QueryBuilder.select(SR_DOCID)
            .from(DataSource.database(baseTestDb))
            .where(Expression.property("string").isNot(Expression.string("string1")))

        val numRows = verifyQuery(query) { _, result ->
            val docID = result.getString(0)!!
            assertEquals(doc1.id, docID)
            val doc = baseTestDb.getDocument(docID)!!
            assertEquals(doc1.getValue("string"), doc.getValue("string"))
        }
        assertEquals(1, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testWhereBetween() {
        loadNumberedDocs(10)
        runTestCases(
            TestCase(
                EXPR_NUMBER1.between(Expression.intValue(3), Expression.intValue(7)),
                3, 4, 5, 6, 7
            )
        )
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testWhereIn() {
        loadJSONResource("names_100.json")

        val expected = arrayOf(
            Expression.string("Marcy"),
            Expression.string("Margaretta"),
            Expression.string("Margrett"),
            Expression.string("Marlen"),
            Expression.string("Maryjo")
        )

        val query = QueryBuilder.select(SelectResult.property("name.first"))
            .from(DataSource.database(baseTestDb))
            .where(Expression.property("name.first").`in`(*expected))
            .orderBy(Ordering.property("name.first"))

        val numRows = verifyQuery(query) { n, result ->
            val name = result.getString(0)
            assertEquals(expected[n - 1].asJSON(), name)
        }
        assertEquals(expected.size, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testWhereLike() {
        loadJSONResource("names_100.json")

        val w = Expression.property("name.first").like(Expression.string("%Mar%"))
        val query = QueryBuilder.select(SR_DOCID)
            .from(DataSource.database(baseTestDb))
            .where(w)
            .orderBy(Ordering.property("name.first").ascending())

        val firstNames = mutableListOf<String>()
        val numRows = verifyQuery(query, false) { _, result ->
            val docID = result.getString(0)
            val doc = baseTestDb.getDocument(docID!!)
            val name = doc!!.getDictionary("name")!!.toMap()
            val firstName = name["first"] as String?
            if (firstName != null) {
                firstNames.add(firstName)
            }
        }
        assertEquals(5, numRows)
        assertEquals(5, firstNames.size)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testWhereRegex() {
        loadJSONResource("names_100.json")

        val query = QueryBuilder.select(SR_DOCID)
            .from(DataSource.database(baseTestDb))
            .where(Expression.property("name.first").regex(Expression.string("^Mar.*")))
            .orderBy(Ordering.property("name.first").ascending())

        val firstNames = mutableListOf<String>()
        val numRows = verifyQuery(query, false) { _, result ->
            val docID = result.getString(0)
            val doc = baseTestDb.getDocument(docID!!)
            val name = doc!!.getDictionary("name")!!.toMap()
            val firstName = name["first"] as String?
            if (firstName != null) {
                firstNames.add(firstName)
            }
        }
        assertEquals(5, numRows)
        assertEquals(5, firstNames.size)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testWhereMatch() {
        loadJSONResource("sentences.json")

        baseTestDb.createIndex(
            "sentence",
            IndexBuilder.fullTextIndex(FullTextIndexItem.property("sentence"))
        )

        val query = QueryBuilder.select(SR_DOCID, SelectResult.property("sentence"))
            .from(DataSource.database(baseTestDb))
            .where(FullTextFunction.match("sentence", "'Dummie woman'"))
            .orderBy(Ordering.expression(FullTextFunction.rank("sentence")).descending())

        val numRows = verifyQuery(query) { _, result ->
            assertNotNull(result.getString(0))
            assertNotNull(result.getString(1))
        }
        assertEquals(2, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testFullTextIndexConfig() {
        loadJSONResource("sentences.json")

        val idxConfig = FullTextIndexConfiguration("sentence", "nonesense")
        assertFalse(idxConfig.isIgnoringAccents)
        assertEquals("en", idxConfig.language)

        idxConfig.setLanguage("en-ca").ignoreAccents(true)
        assertEquals("en-ca", idxConfig.language)
        assertTrue(idxConfig.isIgnoringAccents)

        baseTestDb.createIndex("sentence", idxConfig)

        val query = QueryBuilder.select(SR_DOCID, SelectResult.property("sentence"))
            .from(DataSource.database(baseTestDb))
            .where(FullTextFunction.match("sentence", "'Dummie woman'"))
            .orderBy(Ordering.expression(FullTextFunction.rank("sentence")).descending())

        val numRows = verifyQuery(query) { n, result ->
            assertNotNull(result.getString(0))
            assertNotNull(result.getString(1))
        }
        assertEquals(2, numRows)
    }

    // Test courtesy of Jayahari Vavachan
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testN1QLFTSQuery() {
        loadJSONResource("sentences.json")

        baseTestDb.createIndex(
            "sentence",
            IndexBuilder.fullTextIndex(FullTextIndexItem.property("sentence"))
        )

        val numRows = verifyQuery(
            baseTestDb.createQuery("SELECT _id FROM _default WHERE MATCH(sentence, 'Dummie woman')")
        ) { _, result ->
            assertNotNull(result.getString(0))
        }

        assertEquals(2, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testOrderBy() {
        loadJSONResource("names_100.json")

        val order = Ordering.expression(Expression.property("name.first"))

        testOrdered(order.ascending(), naturalOrder())
        testOrdered(order.descending(), reverseOrder())
    }

    // https://github.com/couchbase/couchbase-lite-ios/issues/1669
    // https://github.com/couchbase/couchbase-lite-core/issues/81
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testSelectDistinct() {
        val doc1 = MutableDocument()
        doc1.setValue("number", 20)
        saveDocInBaseTestDb(doc1)

        val doc2 = MutableDocument()
        doc2.setValue("number", 20)
        saveDocInBaseTestDb(doc2)

        val S_NUMBER = SelectResult.property("number")
        val query = QueryBuilder.selectDistinct(S_NUMBER).from(DataSource.database(baseTestDb))

        val numRows = verifyQuery(query) { _, result ->
            assertEquals(20, result.getInt(0))
        }
        assertEquals(1, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testJoin() {
        loadNumberedDocs(100)

        val doc1 = MutableDocument("joinme")
        doc1.setValue("theone", 42)
        saveDocInBaseTestDb(doc1)

        val join = Join.join(DataSource.database(baseTestDb).`as`("secondary"))
            .on(
                Expression.property("number1").from("main")
                    .equalTo(Expression.property("theone").from("secondary"))
            )

        val query = QueryBuilder.select(SelectResult.expression(Meta.id.from("main")))
            .from(DataSource.database(baseTestDb).`as`("main"))
            .join(join)

        val numRows = verifyQuery(query) { _, result ->
            val docID = result.getString(0)!!
            val doc = baseTestDb.getDocument(docID)!!
            assertEquals(42, doc.getInt("number1"))
        }
        assertEquals(1, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testLeftJoin() {
        loadNumberedDocs(100)

        val joinme = MutableDocument("joinme")
        joinme.setValue("theone", 42)
        saveDocInBaseTestDb(joinme)

        //Expression mainPropExpr =

        val query = QueryBuilder.select(
            SelectResult.expression(Expression.property("number2").from("main")),
            SelectResult.expression(Expression.property("theone").from("secondary"))
        )
            .from(DataSource.database(baseTestDb).`as`("main"))
            .join(
                Join.leftJoin(DataSource.database(baseTestDb).`as`("secondary"))
                    .on(
                        Expression.property("number1").from("main")
                            .equalTo(Expression.property("theone").from("secondary"))
                    )
            )

        val numRows = verifyQuery(query) { n, result ->
            if (n == 41) {
                assertEquals(59, result.getInt(0))
                assertNull(result.getValue(1))
            }
            if (n == 42) {
                assertEquals(58, result.getInt(0))
                assertEquals(42, result.getInt(1))
            }
        }
        assertEquals(101, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testCrossJoin() {
        loadNumberedDocs(10)

        val query = QueryBuilder.select(
            SelectResult.expression(Expression.property("number1").from("main")),
            SelectResult.expression(Expression.property("number2").from("secondary"))
        )
            .from(DataSource.database(baseTestDb).`as`("main"))
            .join(Join.crossJoin(DataSource.database(baseTestDb).`as`("secondary")))

        val numRows = verifyQuery(query) { n, result ->
            val num1 = result.getInt(0)
            val num2 = result.getInt(1)
            assertEquals((num1 - 1) % 10, (n - 1) / 10)
            assertEquals((10 - num2) % 10, n % 10)
        }
        assertEquals(100, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGroupBy() {
        loadJSONResource("names_100.json")

        val expectedStates = listOf("AL", "CA", "CO", "FL", "IA")
        val expectedCounts = listOf<Long>(1, 6, 1, 1, 3)
        val expectedMaxZips = listOf("35243", "94153", "81223", "33612", "50801")

        val ds = DataSource.database(baseTestDb)

        val state = Expression.property("contact.address.state")
        val count = Function.count(Expression.intValue(1))
        val zip = Expression.property("contact.address.zip")
        val maxZip = Function.max(zip)
        val gender = Expression.property("gender")

        val rsState = SelectResult.property("contact.address.state")
        val rsCount = SelectResult.expression(count)
        val rsMaxZip = SelectResult.expression(maxZip)

        val ordering = Ordering.expression(state)

        var query = QueryBuilder.select(rsState, rsCount, rsMaxZip)
            .from(ds)
            .where(gender.equalTo(Expression.string("female")))
            .groupBy(state)
            .orderBy(ordering)

        var numRows = verifyQuery(query) { n, result ->
            val state1 = result.getValue(0) as String
            val count1 = result.getValue(1) as Long
            val maxZip1 = result.getValue(2) as String
            if (n - 1 < expectedStates.size) {
                assertEquals(expectedStates[n - 1], state1)
                assertEquals(expectedCounts[n - 1], count1)
                assertEquals(expectedMaxZips[n - 1], maxZip1)
            }
        }
        assertEquals(31, numRows)

        // With HAVING:
        val expectedStates2 = listOf("CA", "IA", "IN")
        val expectedCounts2 = listOf<Long>(6, 3, 2)
        val expectedMaxZips2 = listOf("94153", "50801", "47952")

        val havingExpr = count.greaterThan(Expression.intValue(1))

        query = QueryBuilder.select(rsState, rsCount, rsMaxZip)
            .from(ds)
            .where(gender.equalTo(Expression.string("female")))
            .groupBy(state)
            .having(havingExpr)
            .orderBy(ordering)

        numRows = verifyQuery(query) { n, result ->
            val state12 = result.getValue(0) as String
            val count12 = result.getValue(1) as Long
            val maxZip12 = result.getValue(2) as String
            if (n - 1 < expectedStates2.size) {
                assertEquals(expectedStates2[n - 1], state12)
                assertEquals(expectedCounts2[n - 1], count12)
                assertEquals(expectedMaxZips2[n - 1], maxZip12)
            }
        }
        assertEquals(15, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testParameters() {
        loadNumberedDocs(100)

        val query = QueryBuilder.select(SR_NUMBER1)
            .from(DataSource.database(baseTestDb))
            .where(EXPR_NUMBER1.between(Expression.parameter("num1"), Expression.parameter("num2")))
            .orderBy(Ordering.expression(EXPR_NUMBER1))

        val params = Parameters(query.parameters)
            .setValue("num1", 2)
            .setValue("num2", 5)
        query.parameters = params

        val expectedNumbers = longArrayOf(2, 3, 4, 5)
        val numRows = verifyQuery(query) { n, result ->
            assertEquals(expectedNumbers[n - 1], result.getValue(0) as Long)
        }
        assertEquals(4, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testMeta() {
        loadNumberedDocs(5)

        val query = QueryBuilder.select(SR_DOCID, SR_SEQUENCE, SR_REVID, SR_NUMBER1)
            .from(DataSource.database(baseTestDb))
            .orderBy(Ordering.expression(Meta.sequence))

        val expectedDocIDs = arrayOf("doc1", "doc2", "doc3", "doc4", "doc5")

        val numRows = verifyQuery(query) { n, result ->
            val docID1 = result.getValue(0) as String
            val docID2 = result.getString(0)
            val docID3 = result.getValue("id") as String
            val docID4 = result.getString("id")

            val seq1 = result.getValue(1) as Long
            val seq2 = result.getLong(1)
            val seq3 = result.getValue("sequence") as Long
            val seq4 = result.getLong("sequence")

            val revId1 = result.getValue(2) as String
            val revId2 = result.getString(2)
            val revId3 = result.getValue("revisionID") as String
            val revId4 = result.getString("revisionID")

            val number = result.getValue(3) as Long

            assertEquals(docID1, docID2)
            assertEquals(docID2, docID3)
            assertEquals(docID3, docID4)
            assertEquals(docID4, expectedDocIDs[n - 1])

            assertEquals(n.toLong(), seq1)
            assertEquals(n.toLong(), seq2)
            assertEquals(n.toLong(), seq3)
            assertEquals(n.toLong(), seq4)

            assertEquals(revId1, revId2)
            assertEquals(revId2, revId3)
            assertEquals(revId3, revId4)
            assertEquals(revId4, baseTestDb.getDocument(docID1)!!.revisionID)

            assertEquals(n.toLong(), number)
        }
        assertEquals(5, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testRevisionIdInCreate() {
        val doc = MutableDocument()
        baseTestDb.save(doc)

        val query = QueryBuilder.select(SelectResult.expression(Meta.revisionID))
            .from(DataSource.database(baseTestDb))
            .where(Meta.id.equalTo(Expression.string(doc.id)))

        val numRows = verifyQuery(query) { _, result ->
            assertEquals(doc.revisionID, result.getString(0))
        }

        assertEquals(1, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testRevisionIdInUpdate() {
        var doc = MutableDocument()
        baseTestDb.save(doc)

        doc = baseTestDb.getDocument(doc.id)!!.toMutable()
        doc.setString("DEC", "Maynard")
        baseTestDb.save(doc)
        val revId = doc.revisionID

        val query = QueryBuilder.select(SelectResult.expression(Meta.revisionID))
            .from(DataSource.database(baseTestDb))
            .where(Meta.id.equalTo(Expression.string(doc.id)))

        val numRows = verifyQuery(query) { _, result ->
            assertEquals(revId, result.getString(0))
        }

        assertEquals(1, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testRevisionIdInWhere() {
        val doc = MutableDocument()
        baseTestDb.save(doc)

        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(baseTestDb))
            .where(Meta.revisionID.equalTo(Expression.string(doc.revisionID)))

        val numRows = verifyQuery(query) { _, result ->
            assertEquals(doc.id, result.getString(0))
        }

        assertEquals(1, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testRevisionIdInDelete() {
        val doc = MutableDocument()
        baseTestDb.save(doc)

        val dbDoc = baseTestDb.getDocument(doc.id)
        assertNotNull(dbDoc)

        baseTestDb.delete(dbDoc)

        val query = QueryBuilder.select(SelectResult.expression(Meta.revisionID))
            .from(DataSource.database(baseTestDb))
            .where(Meta.deleted.equalTo(Expression.booleanValue(true)))

        val numRows = verifyQuery(query) { _, result ->
            assertEquals(dbDoc.revisionID, result.getString(0))
        }

        assertEquals(1, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testLimit() {
        loadNumberedDocs(10)

        val dataSource = DataSource.database(baseTestDb)

        var query = QueryBuilder.select(SR_NUMBER1)
            .from(dataSource)
            .orderBy(Ordering.expression(EXPR_NUMBER1))
            .limit(Expression.intValue(5))

        val expectedNumbers = longArrayOf(1, 2, 3, 4, 5)
        var numRows = verifyQuery(query) { n, result ->
            val number = result.getValue(0) as Long
            assertEquals(expectedNumbers[n - 1], number)
        }
        assertEquals(5, numRows)

        val paramExpr = Expression.parameter("LIMIT_NUM")
        query = QueryBuilder.select(SR_NUMBER1)
            .from(dataSource)
            .orderBy(Ordering.expression(EXPR_NUMBER1))
            .limit(paramExpr)
        val params = Parameters(query.parameters).setValue("LIMIT_NUM", 3)
        query.parameters = params

        val expectedNumbers2 = longArrayOf(1, 2, 3)
        numRows = verifyQuery(query) { n, result ->
            val number = result.getValue(0) as Long
            assertEquals(expectedNumbers2[n - 1], number)
        }
        assertEquals(3, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testLimitOffset() {
        loadNumberedDocs(10)

        val dataSource = DataSource.database(baseTestDb)

        var query = QueryBuilder.select(SR_NUMBER1)
            .from(dataSource)
            .orderBy(Ordering.expression(EXPR_NUMBER1))
            .limit(Expression.intValue(5), Expression.intValue(3))

        val expectedNumbers = longArrayOf(4, 5, 6, 7, 8)
        var numRows = verifyQuery(query) { n, result ->
            assertEquals(expectedNumbers[n - 1], result.getValue(0) as Long)
        }
        assertEquals(5, numRows)

        val paramLimitExpr = Expression.parameter("LIMIT_NUM")
        val paramOffsetExpr = Expression.parameter("OFFSET_NUM")
        query = QueryBuilder.select(SR_NUMBER1)
            .from(dataSource)
            .orderBy(Ordering.expression(EXPR_NUMBER1))
            .limit(paramLimitExpr, paramOffsetExpr)
        val params = Parameters(query.parameters)
            .setValue("LIMIT_NUM", 3)
            .setValue("OFFSET_NUM", 5)
        query.parameters = params

        val expectedNumbers2 = longArrayOf(6, 7, 8)
        numRows = verifyQuery(query) { n, result ->
            assertEquals(expectedNumbers2[n - 1], result.getValue(0) as Long)
        }
        assertEquals(3, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testQueryResult() {
        loadJSONResource("names_100.json")

        val query = QueryBuilder.select(
            SelectResult.property("name.first").`as`("firstname"),
            SelectResult.property("name.last").`as`("lastname"),
            SelectResult.property("gender"),
            SelectResult.property("contact.address.city")
        )
            .from(DataSource.database(baseTestDb))

        val numRows = verifyQuery(query) { _, result ->
            assertEquals(4, result.count())
            assertEquals(result.getValue(0), result.getValue("firstname"))
            assertEquals(result.getValue(1), result.getValue("lastname"))
            assertEquals(result.getValue(2), result.getValue("gender"))
            assertEquals(result.getValue(3), result.getValue("city"))
        }
        assertEquals(100, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testQueryProjectingKeys() {
        loadNumberedDocs(100)

        val query = QueryBuilder.select(
            SelectResult.expression(Function.avg(EXPR_NUMBER1)),
            SelectResult.expression(Function.count(EXPR_NUMBER1)),
            SelectResult.expression(Function.min(EXPR_NUMBER1)).`as`("min"),
            SelectResult.expression(Function.max(EXPR_NUMBER1)),
            SelectResult.expression(Function.sum(EXPR_NUMBER1)).`as`("sum")
        )
            .from(DataSource.database(baseTestDb))

        val numRows = verifyQuery(query) { _, result ->
            assertEquals(5, result.count)
            assertEquals(result.getValue(0), result.getValue("$1"))
            assertEquals(result.getValue(1), result.getValue("$2"))
            assertEquals(result.getValue(2), result.getValue("min"))
            assertEquals(result.getValue(3), result.getValue("$3"))
            assertEquals(result.getValue(4), result.getValue("sum"))
        }
        assertEquals(1, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testQuantifiedOperators() {
        loadJSONResource("names_100.json")

        val ds = DataSource.database(baseTestDb)

        val exprLikes = Expression.property("likes")
        val exprVarLike = ArrayExpression.variable("LIKE")

        // ANY:
        var query = QueryBuilder.select(SR_DOCID)
            .from(ds)
            .where(
                ArrayExpression.any(exprVarLike)
                    .`in`(exprLikes)
                    .satisfies(exprVarLike.equalTo(Expression.string("climbing")))
            )

        val i = atomic(0)
        val expected = arrayOf("doc-017", "doc-021", "doc-023", "doc-045", "doc-060")
        var numRows = verifyQuery(query, false) { _, result ->
            assertEquals(expected[i.getAndIncrement()], result.getString(0))
        }
        assertEquals(expected.size, numRows)

        // EVERY:
        query = QueryBuilder.select(SR_DOCID)
            .from(ds)
            .where(
                ArrayExpression.every(ArrayExpression.variable("LIKE"))
                    .`in`(exprLikes)
                    .satisfies(exprVarLike.equalTo(Expression.string("taxes")))
            )

        numRows = verifyQuery(query, false) { n, result ->
            if (n == 1) {
                assertEquals("doc-007", result.getString(0))
            }
        }
        assertEquals(42, numRows)

        // ANY AND EVERY:
        query = QueryBuilder.select(SR_DOCID)
            .from(ds)
            .where(
                ArrayExpression.anyAndEvery(ArrayExpression.variable("LIKE"))
                    .`in`(exprLikes)
                    .satisfies(exprVarLike.equalTo(Expression.string("taxes")))
            )

        numRows = verifyQuery(query, false) { _, _ -> }
        assertEquals(0, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testAggregateFunctions() {
        loadNumberedDocs(100)

        val query = QueryBuilder.select(
            SelectResult.expression(Function.avg(EXPR_NUMBER1)),
            SelectResult.expression(Function.count(EXPR_NUMBER1)),
            SelectResult.expression(Function.min(EXPR_NUMBER1)),
            SelectResult.expression(Function.max(EXPR_NUMBER1)),
            SelectResult.expression(Function.sum(EXPR_NUMBER1))
        )
            .from(DataSource.database(baseTestDb))
        val numRows = verifyQuery(query) { _, result ->
            assertEquals(50.5f, result.getValue(0) as Float, 0.0f)
            assertEquals(100L, result.getValue(1) as Long)
            assertEquals(1L, result.getValue(2) as Long)
            assertEquals(100L, result.getValue(3) as Long)
            assertEquals(5050L, result.getValue(4) as Long)
        }
        assertEquals(1, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testArrayFunctions() {
        val doc = MutableDocument("doc1")
        val array = MutableArray()
        array.addValue("650-123-0001")
        array.addValue("650-123-0002")
        doc.setValue("array", array)
        saveDocInBaseTestDb(doc)

        val ds = DataSource.database(baseTestDb)

        val exprArray = Expression.property("array")

        var query = QueryBuilder.select(SelectResult.expression(ArrayFunction.length(exprArray)))
            .from(ds)

        var numRows = verifyQuery(query) { _, result ->
            assertEquals(2, result.getInt(0))
        }
        assertEquals(1, numRows)

        query = QueryBuilder.select(
            SelectResult.expression(
                ArrayFunction.contains(exprArray, Expression.string("650-123-0001"))
            ),
            SelectResult.expression(
                ArrayFunction.contains(exprArray, Expression.string("650-123-0003"))
            )
        )
            .from(ds)

        numRows = verifyQuery(query) { _, result ->
            assertTrue(result.getBoolean(0))
            assertFalse(result.getBoolean(1))
        }
        assertEquals(1, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testMathFunctions() {
        val key = "number"
        val num = 0.6

        val doc = MutableDocument("doc1")
        doc.setValue(key, num)
        saveDocInBaseTestDb(doc)

        val propNumber = Expression.property(key)

        val fns = arrayOf(
            MathFn("abs", Function.abs(propNumber), abs(num)),
            MathFn("acos", Function.acos(propNumber), acos(num)),
            MathFn("asin", Function.asin(propNumber), asin(num)),
            MathFn("atan", Function.atan(propNumber), atan(num)),
            MathFn(
                "atan2",
                Function.atan2(Expression.doubleValue(90.0), Expression.doubleValue(num)),
                atan2(90.0, num)
            ),
            MathFn("ceil", Function.ceil(propNumber), ceil(num)),
            MathFn("cos", Function.cos(propNumber), cos(num)),
            MathFn("degrees", Function.degrees(propNumber), num * 180.0 / PI),
            MathFn("exp", Function.exp(propNumber), exp(num)),
            MathFn("floor", Function.floor(propNumber), floor(num)),
            MathFn("ln", Function.ln(propNumber), ln(num)),
            MathFn("log10", Function.log(propNumber), log10(num)),
            MathFn("pow", Function.power(propNumber, Expression.intValue(2)), num.pow(2.0)),
            MathFn("rad", Function.radians(propNumber), num * PI / 180.0),
            MathFn("round", Function.round(propNumber), round(num)),
            MathFn(
                "round 10",
                Function.round(propNumber, Expression.intValue(1)),
                round(num * 10.0) / 10.0
            ),
            MathFn("sign", Function.sign(propNumber), 1.0),
            MathFn("sin", Function.sin(propNumber), sin(num)),
            MathFn("sqrt", Function.sqrt(propNumber), sqrt(num)),
            MathFn("tan", Function.tan(propNumber), tan(num)),
            MathFn("trunc", Function.trunc(propNumber), 0.0),
            MathFn("trunc 10", Function.trunc(propNumber, Expression.intValue(1)), 0.6)
        )

        for (f in fns) {
            val nRows = verifyQuery(
                QueryBuilder.select(SelectResult.expression(f.expr))
                    .from(DataSource.database(baseTestDb))
            ) { _, result ->
                assertEquals(f.expected, result.getDouble(0), 1E-12, f.name)
            }
            assertEquals(1, nRows)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testStringFunctions() {
        val str = "  See you 18r  "
        val doc = MutableDocument("doc1")
        doc.setValue("greeting", str)
        saveDocInBaseTestDb(doc)

        val ds = DataSource.database(baseTestDb)

        val prop = Expression.property("greeting")

        // Contains:
        val fnContains1 = Function.contains(prop, Expression.string("8"))
        val fnContains2 = Function.contains(prop, Expression.string("9"))
        val srFnContains1 = SelectResult.expression(fnContains1)
        val srFnContains2 = SelectResult.expression(fnContains2)

        var query = QueryBuilder.select(srFnContains1, srFnContains2).from(ds)

        var numRows = verifyQuery(query) { _, result ->
            assertTrue(result.getBoolean(0))
            assertFalse(result.getBoolean(1))
        }
        assertEquals(1, numRows)

        // Length
        val fnLength = Function.length(prop)
        query = QueryBuilder.select(SelectResult.expression(fnLength)).from(ds)

        numRows = verifyQuery(query) { _, result ->
            assertEquals(str.length, result.getInt(0))
        }
        assertEquals(1, numRows)

        // Lower, Ltrim, Rtrim, Trim, Upper:
        val fnLower = Function.lower(prop)
        val fnLTrim = Function.ltrim(prop)
        val fnRTrim = Function.rtrim(prop)
        val fnTrim = Function.trim(prop)
        val fnUpper = Function.upper(prop)

        query = QueryBuilder.select(
            SelectResult.expression(fnLower),
            SelectResult.expression(fnLTrim),
            SelectResult.expression(fnRTrim),
            SelectResult.expression(fnTrim),
            SelectResult.expression(fnUpper)
        )
            .from(ds)

        numRows = verifyQuery(query) { _, result ->
            assertEquals(str.lowercase(), result.getString(0))
            assertEquals(str.replace("^\\s+".toRegex(), ""), result.getString(1))
            assertEquals(str.replace("\\s+$".toRegex(), ""), result.getString(2))
            assertEquals(str.trim { it <= ' ' }, result.getString(3))
            assertEquals(str.uppercase(), result.getString(4))
        }
        assertEquals(1, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testSelectAll() {
        loadNumberedDocs(100)

        val ds = DataSource.database(baseTestDb)
        val dbName = baseTestDb.name

        // SELECT *
        var query = QueryBuilder.select(SR_ALL).from(ds)

        var numRows = verifyQuery(query) { n, result ->
            assertEquals(1, result.count)
            val a1 = result.getDictionary(0)
            val a2 = result.getDictionary(dbName)
            assertEquals(n, a1!!.getInt("number1"))
            assertEquals((100 - n), a1.getInt("number2"))
            assertEquals(n, a2!!.getInt("number1"))
            assertEquals((100 - n), a2.getInt("number2"))
        }
        assertEquals(100, numRows)

        // SELECT *, number1
        query = QueryBuilder.select(SR_ALL, SR_NUMBER1).from(ds)

        numRows = verifyQuery(query) { n, result ->
            assertEquals(2, result.count)
            val a1 = result.getDictionary(0)
            val a2 = result.getDictionary(dbName)
            assertEquals(n, a1!!.getInt("number1"))
            assertEquals((100 - n), a1.getInt("number2"))
            assertEquals(n, a2!!.getInt("number1"))
            assertEquals((100 - n), a2.getInt("number2"))
            assertEquals(n, result.getInt(1))
            assertEquals(n, result.getInt("number1"))
        }
        assertEquals(100, numRows)

        // SELECT testdb.*
        query = QueryBuilder.select(SelectResult.all().from(dbName)).from(ds.`as`(dbName))
        numRows = verifyQuery(query) { n, result ->
            assertEquals(1, result.count)
            val a1 = result.getDictionary(0)
            val a2 = result.getDictionary(dbName)
            assertEquals(n, a1!!.getInt("number1"))
            assertEquals((100 - n), a1.getInt("number2"))
            assertEquals(n, a2!!.getInt("number1"))
            assertEquals((100 - n), a2.getInt("number2"))
        }
        assertEquals(100, numRows)

        // SELECT testdb.*, testdb.number1
        query = QueryBuilder.select(
            SelectResult.all().from(dbName),
            SelectResult.expression(Expression.property("number1").from(dbName))
        )
            .from(ds.`as`(dbName))

        numRows = verifyQuery(query) { n, result ->
            assertEquals(2, result.count)
            val a1 = result.getDictionary(0)
            val a2 = result.getDictionary(dbName)
            assertEquals(n, a1!!.getInt("number1"))
            assertEquals((100 - n), a1.getInt("number2"))
            assertEquals(n, a2!!.getInt("number1"))
            assertEquals((100 - n), a2.getInt("number2"))
            assertEquals(n, result.getInt(1))
            assertEquals(n, result.getInt("number1"))
        }
        assertEquals(100, numRows)
    }

    // With no locale, characters with diacritics should be
    // treated as the original letters A, E, I, O, U,
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testUnicodeCollationWithLocaleNone() {
        createAlphaDocs()

        val noLocale: Collation = Collation.unicode()
            .setLocale(null)
            .setIgnoreCase(false)
            .setIgnoreAccents(false)

        val query = QueryBuilder.select(SelectResult.property("string"))
            .from(DataSource.database(baseTestDb))
            .orderBy(Ordering.expression(Expression.property("string").collate(noLocale)))

        val expected = arrayOf("A", "Å", "B", "Z")
        val numRows = verifyQuery(query) { n, result ->
            assertEquals(expected[n - 1], result.getString(0))
        }
        assertEquals(expected.size, numRows)
    }

    // In the Spanish alphabet, the six characters with diacritics Á, É, Í, Ó, Ú, Ü
    // are treated as the original letters A, E, I, O, U,
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testUnicodeCollationWithLocaleSpanish() {
        createAlphaDocs()

        val localeEspanol: Collation = Collation.unicode()
            .setLocale("es")
            .setIgnoreCase(false)
            .setIgnoreAccents(false)

        val query = QueryBuilder.select(SelectResult.property("string"))
            .from(DataSource.database(baseTestDb))
            .orderBy(Ordering.expression(Expression.property("string").collate(localeEspanol)))

        val expected = arrayOf("A", "Å", "B", "Z")
        val numRows = verifyQuery(query) { n, result ->
            assertEquals(expected[n - 1], result.getString(0))
        }
        assertEquals(expected.size, numRows)
    }

    // In the Swedish alphabet, there are three extra vowels
    // placed at its end (..., X, Y, Z, Å, Ä, Ö),
    // Early versions of Android do not support the Swedish Locale
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testUnicodeCollationWithLocaleSwedish() {
        createAlphaDocs()

        val query = QueryBuilder.select(SelectResult.property("string"))
            .from(DataSource.database(baseTestDb))
            .orderBy(
                Ordering.expression(
                    Expression.property("string").collate(
                        Collation.unicode().setLocale("sv")
                            .setIgnoreCase(false)
                            .setIgnoreAccents(false)
                    )
                )
            )

        val expected = arrayOf("A", "B", "Z", "Å")
        val numRows = verifyQuery(query) { n, result ->
            assertEquals(expected[n - 1], result.getString(0))
        }
        assertEquals(expected.size, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testCompareWithUnicodeCollation() {
        class CollationTest(
            val value: String,
            val test: String,
            val mode: Boolean,
            val collation: Collation
        ) {
            override fun toString(): String =
                "test '" + value + "' " + (if (mode) "=" else "<") + " '" + test + "'"
        }

        val bothSensitive =
            Collation.unicode().setLocale(null).setIgnoreCase(false).setIgnoreAccents(false)
        val accentSensitive =
            Collation.unicode().setLocale(null).setIgnoreCase(true).setIgnoreAccents(false)
        val caseSensitive =
            Collation.unicode().setLocale(null).setIgnoreCase(false).setIgnoreAccents(true)
        val noSensitive =
            Collation.unicode().setLocale(null).setIgnoreCase(true).setIgnoreAccents(true)

        val testData = listOf(
            // Edge cases: empty and 1-char strings:
            CollationTest("", "", true, bothSensitive),
            CollationTest("", "a", false, bothSensitive),
            CollationTest("a", "a", true, bothSensitive),

            // Case sensitive: lowercase come first by unicode rules:
            CollationTest("a", "A", false, bothSensitive),
            CollationTest("abc", "abc", true, bothSensitive),
            CollationTest("Aaa", "abc", false, bothSensitive),
            CollationTest("abc", "abC", false, bothSensitive),
            CollationTest("AB", "abc", false, bothSensitive),

            // Case insensitive:
            CollationTest("ABCDEF", "ZYXWVU", false, accentSensitive),
            CollationTest("ABCDEF", "Z", false, accentSensitive),

            CollationTest("a", "A", true, accentSensitive),
            CollationTest("abc", "ABC", true, accentSensitive),
            CollationTest("ABA", "abc", false, accentSensitive),

            CollationTest("commonprefix1", "commonprefix2", false, accentSensitive),
            CollationTest("commonPrefix1", "commonprefix2", false, accentSensitive),

            CollationTest("abcdef", "abcdefghijklm", false, accentSensitive),
            CollationTest("abcdeF", "abcdefghijklm", false, accentSensitive),

            // Now bring in non-ASCII characters:
            CollationTest("a", "á", false, accentSensitive),
            CollationTest("", "á", false, accentSensitive),
            CollationTest("á", "á", true, accentSensitive),
            CollationTest("•a", "•A", true, accentSensitive),

            CollationTest("test a", "test á", false, accentSensitive),
            CollationTest("test á", "test b", false, accentSensitive),
            CollationTest("test á", "test Á", true, accentSensitive),
            CollationTest("test á1", "test Á2", false, accentSensitive),

            // Case sensitive, diacritic sensitive:
            CollationTest("ABCDEF", "ZYXWVU", false, bothSensitive),
            CollationTest("ABCDEF", "Z", false, bothSensitive),
            CollationTest("a", "A", false, bothSensitive),
            CollationTest("abc", "ABC", false, bothSensitive),
            CollationTest("•a", "•A", false, bothSensitive),
            CollationTest("test a", "test á", false, bothSensitive),
            CollationTest("Ähnlichkeit", "apple", false, bothSensitive),

            // Because 'h'-vs-'p' beats 'Ä'-vs-'a'
            CollationTest("ax", "Äz", false, bothSensitive),
            CollationTest("test a", "test Á", false, bothSensitive),
            CollationTest("test Á", "test e", false, bothSensitive),
            CollationTest("test á", "test Á", false, bothSensitive),
            CollationTest("test á", "test b", false, bothSensitive),
            CollationTest("test u", "test Ü", false, bothSensitive),

            // Case sensitive, diacritic insensitive
            CollationTest("abc", "ABC", false, caseSensitive),
            CollationTest("test á", "test a", true, caseSensitive),
            CollationTest("test a", "test á", true, caseSensitive),
            CollationTest("test á", "test A", false, caseSensitive),
            CollationTest("test á", "test b", false, caseSensitive),
            CollationTest("test á", "test Á", false, caseSensitive),

            // Case and diacritic insensitive
            CollationTest("test á", "test Á", true, noSensitive)
        )

        for (data in testData) {
            val mDoc = MutableDocument()
            mDoc.setValue("value", data.value)
            val doc = saveDocInBaseTestDb(mDoc)

            val test = Expression.value(data.test)
            var comparison = Expression.property("value").collate(data.collation)
            comparison = if (data.mode) comparison.equalTo(test) else comparison.lessThan(test)

            val query = QueryBuilder.select().from(DataSource.database(baseTestDb))
                .where(comparison)

            val numRows = verifyQuery(query) { n, result ->
                assertEquals(1, n)
                assertNotNull(result)
            }
            assertEquals(1, numRows, data.toString())

            baseTestDb.delete(doc)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testLiveQuery() = runBlocking {
        loadNumberedDocs(100)

        val query = QueryBuilder.select(SR_DOCID)
            .from(DataSource.database(baseTestDb))
            .where(EXPR_NUMBER1.lessThan(Expression.intValue(10)))
            .orderBy(Ordering.property("number1").ascending())

        val latch = CountDownLatch(2)
        val listener = { change: QueryChange ->
            val rs = change.results!!
            if (latch.getCount() == 2L) {
                var count = 0
                while (rs.next() != null) {
                    count++
                }
                assertEquals(9, count)
            } else if (latch.getCount() == 1L) {
                var count = 0
                for (result in rs) {
                    if (count == 0) {
                        val doc = baseTestDb.getDocument(result.getString(0)!!)!!
                        assertEquals(-1L, doc.getValue("number1"))
                    }
                    count++
                }
                assertEquals(10, count)
            }

            latch.countDown()
        }

        val token = query.addChangeListener(listener)
        try {
            // create one doc
            executeAsync(500) {
                try {
                    createNumberedDocInBaseTestDb(-1, 100)
                } catch (e: CouchbaseLiteException) {
                    throw RuntimeException(e)
                }
            }
            // wait till listener is called
            withTimeout(LONG_TIMEOUT_SEC.seconds) {
                latch.await()
            }
        } finally {
            query.removeChangeListener(token)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testLiveQueryNoUpdate() {
        testLiveQueryNoUpdate(false)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testLiveQueryNoUpdateConsumeAll() {
        testLiveQueryNoUpdate(true)
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1356
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testCountFunctions() {
        loadNumberedDocs(100)

        val ds = DataSource.database(baseTestDb)
        val cnt = Function.count(EXPR_NUMBER1)

        val rsCnt = SelectResult.expression(cnt)
        val query = QueryBuilder.select(rsCnt).from(ds)

        val numRows = verifyQuery(query) { _, result ->
            assertEquals(100L, result.getValue(0) as Long)
        }
        assertEquals(1, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testJoinWithArrayContains() {
        // Data preparation
        // Hotels
        val hotel1 = MutableDocument("hotel1")
        hotel1.setString("type", "hotel")
        hotel1.setString("name", "Hilton")
        baseTestDb.save(hotel1)

        val hotel2 = MutableDocument("hotel2")
        hotel2.setString("type", "hotel")
        hotel2.setString("name", "Sheraton")
        baseTestDb.save(hotel2)

        val hotel3 = MutableDocument("hotel2")
        hotel3.setString("type", "hotel")
        hotel3.setString("name", "Marriott")
        baseTestDb.save(hotel3)

        // Bookmark
        val bookmark1 = MutableDocument("bookmark1")
        bookmark1.setString("type", "bookmark")
        bookmark1.setString("title", "Bookmark For Hawaii")
        val hotels1 = MutableArray()
        hotels1.addString("hotel1")
        hotels1.addString("hotel2")
        bookmark1.setArray("hotels", hotels1)
        baseTestDb.save(bookmark1)

        val bookmark2 = MutableDocument("bookmark2")
        bookmark2.setString("type", "bookmark")
        bookmark2.setString("title", "Bookmark for New York")
        val hotels2 = MutableArray()
        hotels2.addString("hotel3")
        bookmark2.setArray("hotels", hotels2)
        baseTestDb.save(bookmark2)

        // Join Query
        val mainDS = DataSource.database(baseTestDb).`as`("main")
        val secondaryDS = DataSource.database(baseTestDb).`as`("secondary")

        val typeExpr = Expression.property("type").from("main")
        val hotelsExpr = Expression.property("hotels").from("main")
        val hotelIdExpr = Meta.id.from("secondary")
        val joinExpr = ArrayFunction.contains(hotelsExpr, hotelIdExpr)
        val join = Join.join(secondaryDS).on(joinExpr)

        val srMainAll = SelectResult.all().from("main")
        val srSecondaryAll = SelectResult.all().from("secondary")
        val query = QueryBuilder.select(srMainAll, srSecondaryAll)
            .from(mainDS)
            .join(join)
            .where(typeExpr.equalTo(Expression.string("bookmark")))

        verifyQuery(query) { _, result ->
            Report.log("RESULT: " + result.toMap())
        }
    }

    //https://github.com/couchbase/couchbase-lite-android/issues/1785
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testResultToMapWithBoolean() {
        val exam1 = MutableDocument("exam1")
        exam1.setString("exam type", "final")
        exam1.setString("question", "There are 45 states in the US.")
        exam1.setBoolean("answer", false)
        baseTestDb.save(exam1)

        val exam2 = MutableDocument("exam2")
        exam2.setString("exam type", "final")
        exam2.setString("question", "There are 100 senators in the US.")
        exam2.setBoolean("answer", true)
        baseTestDb.save(exam2)

        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(baseTestDb))
            .where(
                Expression.property("exam type").equalTo(Expression.string("final"))
                    .and(Expression.property("answer").equalTo(Expression.booleanValue(true)))
            )

        val dbName = baseTestDb.name
        verifyQuery(query) { _, result ->
            val maps = result.toMap()
            assertNotNull(maps)
            val map = maps[dbName] as Map<*, *>?
            assertNotNull(map)
            if ("There are 45 states in the US." == map["question"]) {
                assertFalse(map["answer"] as Boolean)
            }
            if ("There are 100 senators in the US." == map["question"]) {
                assertTrue(map["answer"] as Boolean)
            }
        }
    }

    //https://github.com/couchbase/couchbase-lite-android-ce/issues/34
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testResultToMapWithBoolean2() {
        val exam1 = MutableDocument("exam1")
        exam1.setString("exam type", "final")
        exam1.setString("question", "There are 45 states in the US.")
        exam1.setBoolean("answer", true)

        baseTestDb.save(exam1)

        val query = QueryBuilder.select(
            SelectResult.property("exam type"),
            SelectResult.property("question"),
            SelectResult.property("answer")
        )
            .from(DataSource.database(baseTestDb))
            .where(Meta.id.equalTo(Expression.string("exam1")))

        verifyQuery(query) { _, result ->
            assertTrue(result.toMap()["answer"] as Boolean)
        }
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1385
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testQueryDeletedDocument() {
        // STEP 1: Insert two documents
        val task1 = createTaskDocument("Task 1", false)
        val task2 = createTaskDocument("Task 2", false)
        assertEquals(2, baseTestDb.count)

        // STEP 2: query documents before deletion
        val query = QueryBuilder.select(SR_DOCID, SR_ALL)
            .from(DataSource.database(baseTestDb))
            .where(Expression.property("type").equalTo(Expression.string("task")))

        var rows = verifyQuery(query) { _, _ -> }
        assertEquals(2, rows)

        // STEP 3: delete task 1
        baseTestDb.delete(task1)
        assertEquals(1, baseTestDb.count)
        assertNull(baseTestDb.getDocument(task1.id))

        // STEP 4: query documents again after deletion
        rows = verifyQuery(query) { _, result ->
            assertEquals(task2.id, result.getString(0))
        }
        assertEquals(1, rows)
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1389
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testQueryWhereBooleanExpression() {
        // STEP 1: Insert three documents
        createTaskDocument("Task 1", false)
        createTaskDocument("Task 2", true)
        createTaskDocument("Task 3", true)
        assertEquals(3, baseTestDb.count)

        val exprType = Expression.property("type")
        val exprComplete = Expression.property("complete")
        val srCount = SelectResult.expression(Function.count(Expression.intValue(1)))

        // regular query - true
        var query = QueryBuilder.select(SR_ALL)
            .from(DataSource.database(baseTestDb))
            .where(
                exprType.equalTo(Expression.string("task"))
                    .and(exprComplete.equalTo(Expression.booleanValue(true)))
            )

        var numRows = verifyQuery(query, false) { _, result ->
            val dict = result.getDictionary(baseTestDb.name)!!
            assertTrue(dict.getBoolean("complete"))
            assertEquals("task", dict.getString("type"))
            assertTrue(dict.getString("title")!!.startsWith("Task "))
        }
        assertEquals(2, numRows)

        // regular query - false
        query = QueryBuilder.select(SR_ALL)
            .from(DataSource.database(baseTestDb))
            .where(
                exprType.equalTo(Expression.string("task"))
                    .and(exprComplete.equalTo(Expression.booleanValue(false)))
            )

        numRows = verifyQuery(query, false) { _, result ->
            val dict = result.getDictionary(baseTestDb.name)!!
            assertFalse(dict.getBoolean("complete"))
            assertEquals("task", dict.getString("type"))
            assertTrue(dict.getString("title")!!.startsWith("Task "))
        }
        assertEquals(1, numRows)

        // aggregation query - true
        query = QueryBuilder.select(srCount)
            .from(DataSource.database(baseTestDb))
            .where(
                exprType.equalTo(Expression.string("task"))
                    .and(exprComplete.equalTo(Expression.booleanValue(true)))
            )

        numRows = verifyQuery(query, false) { _, result ->
            assertEquals(2, result.getInt(0))
        }
        assertEquals(1, numRows)

        // aggregation query - false
        query = QueryBuilder.select(srCount)
            .from(DataSource.database(baseTestDb))
            .where(
                exprType.equalTo(Expression.string("task"))
                    .and(exprComplete.equalTo(Expression.booleanValue(false)))
            )

        numRows = verifyQuery(query, false) { _, result ->
            assertEquals(1, result.getInt(0))
        }
        assertEquals(1, numRows)
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1413
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testJoinAll() {
        loadNumberedDocs(100)

        val doc1 = MutableDocument("joinme")
        doc1.setValue("theone", 42)
        saveDocInBaseTestDb(doc1)

        val mainDS = DataSource.database(baseTestDb).`as`("main")
        val secondaryDS = DataSource.database(baseTestDb).`as`("secondary")

        val mainPropExpr = Expression.property("number1").from("main")
        val secondaryExpr = Expression.property("theone").from("secondary")
        val joinExpr = mainPropExpr.equalTo(secondaryExpr)
        val join = Join.join(secondaryDS).on(joinExpr)

        val MAIN_ALL = SelectResult.all().from("main")
        val SECOND_ALL = SelectResult.all().from("secondary")

        val query = QueryBuilder.select(MAIN_ALL, SECOND_ALL).from(mainDS).join(join)

        val numRows = verifyQuery(query) { _, result ->
            val mainAll1 = result.getDictionary(0)
            val mainAll2 = result.getDictionary("main")
            val secondAll1 = result.getDictionary(1)
            val secondAll2 = result.getDictionary("secondary")
            assertEquals(42, mainAll1!!.getInt("number1"))
            assertEquals(42, mainAll2!!.getInt("number1"))
            assertEquals(58, mainAll1.getInt("number2"))
            assertEquals(58, mainAll2.getInt("number2"))
            assertEquals(42, secondAll1!!.getInt("theone"))
            assertEquals(42, secondAll2!!.getInt("theone"))
        }
        assertEquals(1, numRows)
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1413
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testJoinByDocID() {
        loadNumberedDocs(100)

        val doc1 = MutableDocument("joinme")
        doc1.setValue("theone", 42)
        doc1.setString("numberID", "doc1") // document ID of number documents.
        saveDocInBaseTestDb(doc1)

        val mainDS = DataSource.database(baseTestDb).`as`("main")
        val secondaryDS = DataSource.database(baseTestDb).`as`("secondary")

        val mainPropExpr = Meta.id.from("main")
        val secondaryExpr = Expression.property("numberID").from("secondary")
        val joinExpr = mainPropExpr.equalTo(secondaryExpr)
        val join = Join.join(secondaryDS).on(joinExpr)

        val MAIN_DOC_ID = SelectResult.expression(Meta.id.from("main")).`as`("mainDocID")
        val SECONDARY_DOC_ID = SelectResult.expression(Meta.id.from("secondary"))
            .`as`("secondaryDocID")
        val SECONDARY_THEONE = SelectResult.expression(
            Expression.property("theone").from("secondary")
        )

        val query = QueryBuilder.select(MAIN_DOC_ID, SECONDARY_DOC_ID, SECONDARY_THEONE)
            .from(mainDS).join(join)

        val numRows = verifyQuery(query) { n, result ->
            assertEquals(1, n)
            val docID = result.getString("mainDocID")
            val doc = baseTestDb.getDocument(docID!!)
            assertEquals(1, doc!!.getInt("number1"))
            assertEquals(99, doc.getInt("number2"))

            // data from secondary
            assertEquals("joinme", result.getString("secondaryDocID"))
            assertEquals(42, result.getInt("theone"))
        }
        assertEquals(1, numRows)
    }

    // TODO: iOS sets locale to NSLocale.currentLocale for unicode collations
    //  https://forums.couchbase.com/t/unicode-collation-locale-null-or-device-locale/34103
    @IgnoreIos
    @Test
    fun testGenerateJSONCollation() {
        val collations = arrayOf(
            Collation.ascii().setIgnoreCase(false),
            Collation.ascii().setIgnoreCase(true),
            Collation.unicode().setLocale(null).setIgnoreCase(false).setIgnoreAccents(false),
            Collation.unicode().setLocale(null).setIgnoreCase(true).setIgnoreAccents(false),
            Collation.unicode().setLocale(null).setIgnoreCase(true).setIgnoreAccents(true),
            Collation.unicode().setLocale("en").setIgnoreCase(false).setIgnoreAccents(false),
            Collation.unicode().setLocale("en").setIgnoreCase(true).setIgnoreAccents(false),
            Collation.unicode().setLocale("en").setIgnoreCase(true).setIgnoreAccents(true)
        )

        val expected = listOf<Map<String, Any?>>(
            mapOf(
                "UNICODE" to false,
                "LOCALE" to null,
                "CASE" to true,
                "DIAC" to true
            ),
            mapOf(
                "UNICODE" to false,
                "LOCALE" to null,
                "CASE" to false,
                "DIAC" to true
            ),
            mapOf(
                "UNICODE" to true,
                "LOCALE" to null,
                "CASE" to true,
                "DIAC" to true
            ),
            mapOf(
                "UNICODE" to true,
                "LOCALE" to null,
                "CASE" to false,
                "DIAC" to true
            ),
            mapOf(
                "UNICODE" to true,
                "LOCALE" to null,
                "CASE" to false,
                "DIAC" to false
            ),
            mapOf(
                "UNICODE" to true,
                "LOCALE" to "en",
                "CASE" to true,
                "DIAC" to true
            ),
            mapOf(
                "UNICODE" to true,
                "LOCALE" to "en",
                "CASE" to false,
                "DIAC" to true
            ),
            mapOf(
                "UNICODE" to true,
                "LOCALE" to "en",
                "CASE" to false,
                "DIAC" to false
            )
        )

        for (i in collations.indices) {
            println("expected[$i] = ${expected[i]}")
            println("collations[$i] = ${collations[i].asJSON()}")
            //assertEquals(expected[i], collations[i].asJSON())
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testAllComparison() {
        val values = arrayOf("Apple", "Aardvark", "Ångström", "Zebra", "äpple")
        for (value in values) {
            val doc = MutableDocument()
            doc.setString("hey", value)
            saveDocInBaseTestDb(doc)
        }
        val testData = listOf(
            listOf(
                "BINARY collation", Collation.ascii(),
                listOf("Aardvark", "Apple", "Zebra", "Ångström", "äpple")
            ),
            listOf(
                "NOCASE collation", Collation.ascii().setIgnoreCase(true),
                listOf("Aardvark", "Apple", "Zebra", "Ångström", "äpple")
            ),
            listOf(
                "Unicode case-sensitive, diacritic-sensitive collation",
                Collation.unicode(),
                listOf("Aardvark", "Ångström", "Apple", "äpple", "Zebra")
            ),
            listOf(
                "Unicode case-INsensitive, diacritic-sensitive collation",
                Collation.unicode().setIgnoreCase(true),
                listOf("Aardvark", "Ångström", "Apple", "äpple", "Zebra")
            ),
            listOf(
                "Unicode case-sensitive, diacritic-INsensitive collation",
                Collation.unicode().setIgnoreAccents(true),
                listOf("Aardvark", "Ångström", "äpple", "Apple", "Zebra")
            ),
            listOf(
                "Unicode case-INsensitive, diacritic-INsensitive collation",
                Collation.unicode().setIgnoreAccents(true).setIgnoreCase(true),
                listOf("Aardvark", "Ångström", "Apple", "äpple", "Zebra")
            )
        )

        val property = Expression.property("hey")
        for (data in testData) {
            val query = QueryBuilder.select(SelectResult.property("hey"))
                .from(DataSource.database(baseTestDb))
                .orderBy(Ordering.expression(property.collate((data[1] as Collation))))

            val list = mutableListOf<String>()
            verifyQuery(query, false) { _, result ->
                list.add(result.getString(0)!!)
            }
            assertEquals(data[2], list)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testDeleteDatabaseWithActiveLiveQuery() = runBlocking {
        val mutex1 = Mutex(true)
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(baseTestDb))

        val token = query.addChangeListener {
            mutex1.unlock()
        }
        try {
            withTimeout(STD_TIMEOUT_SEC.seconds) {
                mutex1.lock()
            }
            baseTestDb.delete()
        } finally {
            query.removeChangeListener(token)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testCloseDatabaseWithActiveLiveQuery() = runBlocking {
        val mutex = Mutex(true)
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(baseTestDb))

        val token = query.addChangeListener {
            mutex.unlock()
        }
        try {
            withTimeout(STD_TIMEOUT_SEC.seconds) {
                mutex.lock()
            }
            baseTestDb.close()
        } finally {
            query.removeChangeListener(token)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testFunctionCount() {
        loadNumberedDocs(100)

        val doc = MutableDocument()
        doc.setValue("string", "STRING")
        doc.setValue("date", null)
        saveDocInBaseTestDb(doc)

        val ds = DataSource.database(baseTestDb)
        val cntNum1 = Function.count(EXPR_NUMBER1)
        val cntInt1 = Function.count(Expression.intValue(1))
        val cntAstr = Function.count(Expression.string("*"))
        val cntAll = Function.count(Expression.all())
        val cntStr = Function.count(Expression.property("string"))
        val cntDate = Function.count(Expression.property("date"))
        val cntNotExist = Function.count(Expression.property("notExist"))

        val rsCntNum1 = SelectResult.expression(cntNum1)
        val rsCntInt1 = SelectResult.expression(cntInt1)
        val rsCntAstr = SelectResult.expression(cntAstr)
        val rsCntAll = SelectResult.expression(cntAll)
        val rsCntStr = SelectResult.expression(cntStr)
        val rsCntDate = SelectResult.expression(cntDate)
        val rsCntNotExist = SelectResult.expression(cntNotExist)

        val query = QueryBuilder
            .select(rsCntNum1, rsCntInt1, rsCntAstr, rsCntAll, rsCntStr, rsCntDate, rsCntNotExist)
            .from(ds)

        val numRows = verifyQuery(query) { _, result ->
            assertEquals(100L, result.getValue(0) as Long)
            assertEquals(101L, result.getValue(1) as Long)
            assertEquals(101L, result.getValue(2) as Long)
            assertEquals(101L, result.getValue(3) as Long)
            assertEquals(1L, result.getValue(4) as Long)
            assertEquals(1L, result.getValue(5) as Long)
            assertEquals(0L, result.getValue(6) as Long)
        }
        assertEquals(1, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testFunctionCountAll() {
        loadNumberedDocs(100)

        val ds = DataSource.database(baseTestDb)
        val dbName = baseTestDb.name

        val countAll = Function.count(Expression.all())
        val countAllFrom = Function.count(Expression.all().from(dbName))
        val srCountAll = SelectResult.expression(countAll)
        val srCountAllFrom = SelectResult.expression(countAllFrom)

        // SELECT count(*)
        var query = QueryBuilder.select(srCountAll).from(ds)
        var numRows = verifyQuery(query) { _, result ->
            assertEquals(1, result.count)
            assertEquals(100L, result.getValue(0) as Long)
        }
        assertEquals(1, numRows)

        // SELECT count(testdb.*)
        query = QueryBuilder.select(srCountAllFrom).from(ds.`as`(dbName))
        numRows = verifyQuery(query) { _, result ->
            assertEquals(1, result.count)
            assertEquals(100L, result.getValue(0) as Long)
        }
        assertEquals(1, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testResultSetEnumeration() {
        loadNumberedDocs(5)

        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(baseTestDb))
            .orderBy(Ordering.property("number1"))

        // Type 1: Enumeration by ResultSet.next()
        var i = 0
        query.execute().use { rs ->
            while (true) {
                val result = rs.next() ?: break
                assertEquals("doc${i + 1}", result.getString(0))
                i++
            }
            assertEquals(5, i)
            assertNull(rs.next())
            assertEquals(0, rs.allResults().size)
        }

        // Type 2: Enumeration by ResultSet.iterator()
        i = 0
        query.execute().use { rs ->
            for (r in rs) {
                assertEquals("doc${i + 1}", r.getString(0))
                i++
            }
            assertEquals(5, i)
            assertNull(rs.next())
            assertEquals(0, rs.allResults().size)
        }

        // Type 3: Enumeration by ResultSet.allResults().get(int index)
        i = 0
        query.execute().use { rs ->
            val list: List<Result> = rs.allResults()
            for (r in list) {
                assertEquals("doc${i + 1}", r.getString(0))
                i++
            }
            assertEquals(5, i)
            assertNull(rs.next())
            assertEquals(0, rs.allResults().size)
        }

        // Type 4: Enumeration by ResultSet.allResults().iterator()
        i = 0
        query.execute().use { rs ->
            for (r in rs.allResults()) {
                assertEquals("doc${i + 1}", r.getString(0))
                i++
            }
            assertEquals(5, i)
            assertNull(rs.next())
            assertEquals(0, rs.allResults().size)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testGetAllResults() {
        loadNumberedDocs(5)

        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(baseTestDb))
            .orderBy(Ordering.property("number1"))

        var results: List<Result>

        // Get all results by get(int)
        var i = 0
        query.execute().use { rs ->
            results = rs.allResults()
            for (j in results.indices) {
                val r = results[j]
                assertEquals("doc${i + 1}", r.getString(0))
                i++
            }
            assertEquals(5, results.size)
            assertEquals(5, i)
            assertNull(rs.next())
            assertEquals(0, rs.allResults().size)
        }

        // Get all results by iterator
        i = 0
        query.execute().use { rs ->
            results = rs.allResults()
            for (r in results) {
                assertEquals("doc${i + 1}", r.getString(0))
                i++
            }
            assertEquals(5, results.size)
            assertEquals(5, i)
            assertNull(rs.next())
            assertEquals(0, rs.allResults().size)
        }

        // Partial enumerating then get all results:
        query.execute().use { rs ->
            assertNotNull(rs.next())
            assertNotNull(rs.next())
            results = rs.allResults()
            i = 2
            for (r in results) {
                assertEquals("doc${i + 1}", r.getString(0))
                i++
            }
            assertEquals(3, results.size)
            assertEquals(5, i)
            assertNull(rs.next())
            assertEquals(0, rs.allResults().size)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testResultSetEnumerationZeroResults() {
        loadNumberedDocs(5)

        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(baseTestDb))
            .where(Expression.property("number1").`is`(Expression.intValue(100)))
            .orderBy(Ordering.property("number1"))

        // Type 1: Enumeration by ResultSet.next()
        var i = 0
        query.execute().use { rs ->
            while (rs.next() != null) {
                i++
            }
            assertEquals(0, i)
            assertNull(rs.next())
            assertEquals(0, rs.allResults().size)
        }

        // Type 2: Enumeration by ResultSet.iterator()
        i = 0
        query.execute().use { rs ->
            for (r in rs) {
                i++
            }
            assertEquals(0, i)
            assertNull(rs.next())
            assertEquals(0, rs.allResults().size)
        }

        // Type 3: Enumeration by ResultSet.allResults().get(int index)
        i = 0
        query.execute().use { rs ->
            val list: List<Result> = rs.allResults()
            for (j in list.indices) {
                list[j]
                i++
            }
            assertEquals(0, i)
            assertNull(rs.next())
            assertEquals(0, rs.allResults().size)
        }

        // Type 4: Enumeration by ResultSet.allResults().iterator()
        i = 0
        query.execute().use { rs ->
            for (r in rs.allResults()) {
                i++
            }
            assertEquals(0, i)
            assertNull(rs.next())
            assertEquals(0, rs.allResults().size)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testMissingValue() {
        val doc1 = MutableDocument("doc1")
        doc1.setValue("name", "Scott")
        doc1.setValue("address", null)
        saveDocInBaseTestDb(doc1)

        val query = QueryBuilder.select(
            SelectResult.property("name"),
            SelectResult.property("address"),
            SelectResult.property("age")
        )
            .from(DataSource.database(baseTestDb))

        // Array:
        verifyQuery(query) { _, result ->
            assertEquals(3, result.count)
            assertEquals("Scott", result.getString(0))
            assertNull(result.getValue(1))
            assertNull(result.getValue(2))
            assertEquals(listOf("Scott", null, null), result.toList())
        }

        // Dictionary:
        verifyQuery(query) { _, result ->
            assertEquals("Scott", result.getString("name"))
            assertNull(result.getString("address"))
            assertTrue(result.contains("address"))
            assertNull(result.getString("age"))
            assertFalse(result.contains("age"))
            val expected = mapOf(
                "name" to "Scott",
                "address" to null
            )
            assertEquals(expected, result.toMap())
        }
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1603
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testExpressionNot() {
        loadNumberedDocs(10)

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.property("number1"))
            .from(DataSource.database(baseTestDb))
            .where(
                Expression.not(
                    Expression.property("number1")
                        .between(Expression.intValue(3), Expression.intValue(5))
                )
            )
            .orderBy(Ordering.expression(Expression.property("number1")).ascending())

        val numRows = verifyQuery(query) { n, result ->
            if (n < 3) {
                assertEquals(n, result.getInt("number1"))
            } else {
                assertEquals(n + 3, result.getInt("number1"))
            }
        }
        assertEquals(7, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testLimitValueIsLargerThanResult() {
        val N = 4
        loadNumberedDocs(N)

        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(baseTestDb))
            .limit(Expression.intValue(10))

        val numRows = verifyQuery(query) { _, _ -> }
        assertEquals(N, numRows)
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1614
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testFTSStemming() {
        val mDoc0 = MutableDocument("doc0")
        mDoc0.setString("content", "hello")
        saveDocInBaseTestDb(mDoc0)

        val mDoc1 = MutableDocument("doc1")
        mDoc1.setString("content", "beauty")
        saveDocInBaseTestDb(mDoc1)

        val mDoc2 = MutableDocument("doc2")
        mDoc2.setString("content", "beautifully")
        saveDocInBaseTestDb(mDoc2)

        val mDoc3 = MutableDocument("doc3")
        mDoc3.setString("content", "beautiful")
        saveDocInBaseTestDb(mDoc3)

        val mDoc4 = MutableDocument("doc4")
        mDoc4.setString("content", "pretty")
        saveDocInBaseTestDb(mDoc4)

        val ftsIndex = IndexBuilder.fullTextIndex(FullTextIndexItem.property("content"))
        ftsIndex.setLanguage("en")
        baseTestDb.createIndex("ftsIndex", ftsIndex)

        val expectedIDs = arrayOf("doc1", "doc2", "doc3")
        val expectedContents = arrayOf("beauty", "beautifully", "beautiful")

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.property("content"))
            .from(DataSource.database(baseTestDb))
            .where(FullTextFunction.match("ftsIndex", "beautiful"))
            .orderBy(Ordering.expression(Meta.id))

        val numRows = verifyQuery(query) { n, result ->
            assertEquals(expectedIDs[n - 1], result.getString("id"))
            assertEquals(expectedContents[n - 1], result.getString("content"))
        }
        assertEquals(3, numRows)
    }

    // https://github.com/couchbase/couchbase-lite-net/blob/master/src/Couchbase.Lite.Tests.Shared/QueryTest.cs#L1721
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testFTSStemming2() {
        baseTestDb.createIndex(
            "passageIndex",
            IndexBuilder.fullTextIndex(FullTextIndexItem.property("passage")).setLanguage("en")
        )
        baseTestDb.createIndex(
            "passageIndexStemless",
            IndexBuilder.fullTextIndex(FullTextIndexItem.property("passage")).setLanguage(null)
        )

        val mDoc1 = MutableDocument("doc1")
        mDoc1.setString("passage", "The boy said to the child, 'Mommy, I want a cat.'")
        saveDocInBaseTestDb(mDoc1)

        val mDoc2 = MutableDocument("doc2")
        mDoc2.setString("passage", "The mother replied 'No, you already have too many cats.'")
        saveDocInBaseTestDb(mDoc2)

        var query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(baseTestDb))
            .where(FullTextFunction.match("passageIndex", "cat"))

        var numRows = verifyQuery(query) { n, result ->
            assertEquals("doc$n", result.getString(0))
        }
        assertEquals(2, numRows)

        query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(baseTestDb))
            .where(FullTextFunction.match("passageIndexStemless", "cat"))

        numRows = verifyQuery(query) { n, result ->
            assertEquals("doc$n", result.getString(0))
        }
        assertEquals(1, numRows)
    }

    // 3.1. Set Operations Using The Enhanced Query Syntax
    // https://www.sqlite.org/fts3.html#_set_operations_using_the_enhanced_query_syntax
    // https://github.com/couchbase/couchbase-lite-android/issues/1620
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testFTSSetOperations() {
        val mDoc1 = MutableDocument("doc1")
        mDoc1.setString("content", "a database is a software system")
        saveDocInBaseTestDb(mDoc1)

        val mDoc2 = MutableDocument("doc2")
        mDoc2.setString("content", "sqlite is a software system")
        saveDocInBaseTestDb(mDoc2)

        val mDoc3 = MutableDocument("doc3")
        mDoc3.setString("content", "sqlite is a database")
        saveDocInBaseTestDb(mDoc3)

        val ftsIndex = IndexBuilder.fullTextIndex(FullTextIndexItem.property("content"))
        baseTestDb.createIndex("ftsIndex", ftsIndex)

        // The enhanced query syntax
        // https://www.sqlite.org/fts3.html#_set_operations_using_the_enhanced_query_syntax

        // AND binary set operator
        var query = QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.property("content"))
            .from(DataSource.database(baseTestDb))
            .where(FullTextFunction.match("ftsIndex", "sqlite AND database"))
            .orderBy(Ordering.expression(Meta.id))

        val expectedIDs = arrayOf("doc3")
        var numRows = verifyQuery(query) { n, result ->
            assertEquals(expectedIDs[n - 1], result.getString("id"))
        }
        assertEquals(expectedIDs.size, numRows)

        // implicit AND operator
        query = QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.property("content"))
            .from(DataSource.database(baseTestDb))
            .where(FullTextFunction.match("ftsIndex", "sqlite database"))
            .orderBy(Ordering.expression(Meta.id))

        val expectedIDs2 = arrayOf("doc3")
        numRows = verifyQuery(query) { n, result ->
            assertEquals(expectedIDs2[n - 1], result.getString("id"))
        }
        assertEquals(expectedIDs2.size, numRows)

        // OR operator
        query = QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.property("content"))
            .from(DataSource.database(baseTestDb))
            .where(FullTextFunction.match("ftsIndex", "sqlite OR database"))
            .orderBy(Ordering.expression(Meta.id))

        val expectedIDs3 = arrayOf("doc1", "doc2", "doc3")
        numRows = verifyQuery(query) { n, result ->
            assertEquals(expectedIDs3[n - 1], result.getString("id"))
        }
        assertEquals(expectedIDs3.size, numRows)

        // NOT operator
        query = QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.property("content"))
            .from(DataSource.database(baseTestDb))
            .where(FullTextFunction.match("ftsIndex", "database NOT sqlite"))
            .orderBy(Ordering.expression(Meta.id))

        val expectedIDs4 = arrayOf("doc1")
        numRows = verifyQuery(query) { n, result ->
            assertEquals(expectedIDs4[n - 1], result.getString("id"))
        }
        assertEquals(expectedIDs4.size, numRows)
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1621
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testFTSMixedOperators() {
        val mDoc1 = MutableDocument("doc1")
        mDoc1.setString("content", "a database is a software system")
        saveDocInBaseTestDb(mDoc1)

        val mDoc2 = MutableDocument("doc2")
        mDoc2.setString("content", "sqlite is a software system")
        saveDocInBaseTestDb(mDoc2)

        val mDoc3 = MutableDocument("doc3")
        mDoc3.setString("content", "sqlite is a database")
        saveDocInBaseTestDb(mDoc3)

        val ftsIndex = IndexBuilder.fullTextIndex(FullTextIndexItem.property("content"))
        baseTestDb.createIndex("ftsIndex", ftsIndex)

        // The enhanced query syntax
        // https://www.sqlite.org/fts3.html#_set_operations_using_the_enhanced_query_syntax

        // A AND B AND C
        var query =
            QueryBuilder.select(SelectResult.expression(Meta.id), SelectResult.property("content"))
                .from(DataSource.database(baseTestDb))
                .where(FullTextFunction.match("ftsIndex", "sqlite AND software AND system"))
                .orderBy(Ordering.expression(Meta.id))

        val expectedIDs = arrayOf("doc2")
        var numRows = verifyQuery(query) { n, result ->
            assertEquals(expectedIDs[n - 1], result.getString("id"))
        }
        assertEquals(expectedIDs.size, numRows)

        // (A AND B) OR C
        query = QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.property("content"))
            .from(DataSource.database(baseTestDb))
            .where(FullTextFunction.match("ftsIndex", "(sqlite AND software) OR database"))
            .orderBy(Ordering.expression(Meta.id))

        val expectedIDs2 = arrayOf("doc1", "doc2", "doc3")
        numRows = verifyQuery(query) { n, result ->
            assertEquals(expectedIDs2[n - 1], result.getString("id"))
        }
        assertEquals(expectedIDs2.size, numRows)

        query = QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.property("content"))
            .from(DataSource.database(baseTestDb))
            .where(FullTextFunction.match("ftsIndex", "(sqlite AND software) OR system"))
            .orderBy(Ordering.expression(Meta.id))

        val expectedIDs3 = arrayOf("doc1", "doc2")
        numRows = verifyQuery(query) { n, result ->
            assertEquals(expectedIDs3[n - 1], result.getString("id"))
        }
        assertEquals(expectedIDs3.size, numRows)

        // (A OR B) AND C
        query = QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.property("content"))
            .from(DataSource.database(baseTestDb))
            .where(FullTextFunction.match("ftsIndex", "(sqlite OR software) AND database"))
            .orderBy(Ordering.expression(Meta.id))

        val expectedIDs4 = arrayOf("doc1", "doc3")
        numRows = verifyQuery(query) { n, result ->
            assertEquals(expectedIDs4[n - 1], result.getString("id"))
        }
        assertEquals(expectedIDs4.size, numRows)

        query = QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.property("content"))
            .from(DataSource.database(baseTestDb))
            .where(FullTextFunction.match("ftsIndex", "(sqlite OR software) AND system"))
            .orderBy(Ordering.expression(Meta.id))

        val expectedIDs5 = arrayOf("doc1", "doc2")
        numRows = verifyQuery(query) { n, result ->
            assertEquals(expectedIDs5[n - 1], result.getString("id"))
        }
        assertEquals(expectedIDs5.size, numRows)

        // A OR B OR C
        query = QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.property("content"))
            .from(DataSource.database(baseTestDb))
            .where(FullTextFunction.match("ftsIndex", "database OR software OR system"))
            .orderBy(Ordering.expression(Meta.id))

        val expectedIDs6 = arrayOf("doc1", "doc2", "doc3")
        numRows = verifyQuery(query) { n, result ->
            assertEquals(expectedIDs6[n - 1], result.getString("id"))
        }
        assertEquals(expectedIDs6.size, numRows)
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1628
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testLiveQueryResultsCount() = runBlocking {
        loadNumberedDocs(50)

        val query = QueryBuilder.select()
            .from(DataSource.database(baseTestDb))
            .where(EXPR_NUMBER1.greaterThan(Expression.intValue(25)))
            .orderBy(Ordering.property("number1").ascending())

        val mutex = Mutex(true)
        val listener = { change: QueryChange ->
            var count = 0
            val rs = change.results
            while (rs?.next() != null) {
                count++
            }
            if (count == 75) { // 26-100
                mutex.unlock()
            }
        }
        val token = query.addChangeListener(listener)

        try {
            // create one doc
            val mutexAdd = Mutex(true)
            executeAsync(500) {
                try {
                    loadNumberedDocs(51, 100)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mutexAdd.unlock()
            }

            withTimeout(LONG_TIMEOUT_SEC.seconds) {
                mutexAdd.lock()
            }
            withTimeout(LONG_TIMEOUT_SEC.seconds) {
                mutex.lock()
            }
        } finally {
            query.removeChangeListener(token)
        }
    }

    // https://forums.couchbase.com/t/how-to-be-notifed-that-document-is-changed-but-livequerys-query-isnt-catching-it-anymore/16199/9
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testLiveQueryNotification() = runBlocking {
        // save doc1 with number1 -> 5
        var doc = MutableDocument("doc1")
        doc.setInt("number1", 5)
        baseTestDb.save(doc)

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.property("number1"))
            .from(DataSource.database(baseTestDb))
            .where(Expression.property("number1").lessThan(Expression.intValue(10)))
            .orderBy(Ordering.property("number1"))

        val mutex1 = Mutex(true)
        val mutex2 = Mutex(true)
        val token = query.addChangeListener { change ->
            var matches = 0
            for (r in change.results!!) {
                matches++
            }

            if (matches == 1) {
                // match doc1 with number1 -> 5 which is less than 10
                mutex1.unlock()
            } else {
                // Not match with doc1 because number1 -> 15 which does not match the query criteria
                mutex2.unlock()
            }
        }

        try {
            withTimeout(STD_TIMEOUT_SEC.seconds) {
                mutex1.lock()
            }

            doc = baseTestDb.getDocument("doc1")!!.toMutable()
            doc.setInt("number1", 15)
            baseTestDb.save(doc)

            withTimeout(STD_TIMEOUT_SEC.seconds) {
                mutex2.lock()
            }
        } finally {
            query.removeChangeListener(token)
        }
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1689
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testQueryAndNLikeOperators() {
        val mDoc1 = MutableDocument("doc1")
        mDoc1.setString("name", "food")
        mDoc1.setString("description", "bar")
        saveDocInBaseTestDb(mDoc1)

        val mDoc2 = MutableDocument("doc2")
        mDoc2.setString("name", "foo")
        mDoc2.setString("description", "unknown")
        saveDocInBaseTestDb(mDoc2)

        val mDoc3 = MutableDocument("doc3")
        mDoc3.setString("name", "water")
        mDoc3.setString("description", "drink")
        saveDocInBaseTestDb(mDoc3)

        val mDoc4 = MutableDocument("doc4")
        mDoc4.setString("name", "chocolate")
        mDoc4.setString("description", "bar")
        saveDocInBaseTestDb(mDoc4)

        // LIKE operator only
        var query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(baseTestDb))
            .where(Expression.property("name").like(Expression.string("%foo%")))
            .orderBy(Ordering.expression(Meta.id))

        var numRows = verifyQuery(query) { n, result ->
            assertEquals(1, result.count)
            if (n == 1) {
                assertEquals("doc1", result.getString(0))
            } else {
                assertEquals("doc2", result.getString(0))
            }
        }
        assertEquals(2, numRows)

        // EQUAL operator only
        query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(baseTestDb))
            .where(Expression.property("description").equalTo(Expression.string("bar")))
            .orderBy(Ordering.expression(Meta.id))

        numRows = verifyQuery(query) { n, result ->
            assertEquals(1, result.count)
            if (n == 1) {
                assertEquals("doc1", result.getString(0))
            } else {
                assertEquals("doc4", result.getString(0))
            }
        }
        assertEquals(2, numRows)

        // AND and LIKE operators
        query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(baseTestDb))
            .where(
                Expression.property("name").like(Expression.string("%foo%"))
                    .and(Expression.property("description").equalTo(Expression.string("bar")))
            )
            .orderBy(Ordering.expression(Meta.id))

        numRows = verifyQuery(query) { _, result ->
            assertEquals(1, result.count)
            assertEquals("doc1", result.getString(0))
        }
        assertEquals(1, numRows)
    }

    // https://forums.couchbase.com/t/
    //     how-to-implement-an-index-join-clause-in-couchbase-lite-2-0-using-objective-c-api/16246
    // https://github.com/couchbase/couchbase-lite-core/issues/497
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testQueryJoinAndSelectAll() {
        loadNumberedDocs(100)

        val joinme = MutableDocument("joinme")
        joinme.setValue("theone", 42)
        saveDocInBaseTestDb(joinme)

        val mainDS = DataSource.database(baseTestDb).`as`("main")
        val secondaryDS = DataSource.database(baseTestDb).`as`("secondary")

        val mainPropExpr = Expression.property("number1").from("main")
        val secondaryExpr = Expression.property("theone").from("secondary")
        val joinExpr = mainPropExpr.equalTo(secondaryExpr)
        val join = Join.leftJoin(secondaryDS).on(joinExpr)

        val sr1 = SelectResult.all().from("main")
        val sr2 = SelectResult.all().from("secondary")

        val query = QueryBuilder.select(sr1, sr2).from(mainDS).join(join)

        val numRows = verifyQuery(query) { n, result ->
            if (n == 41) {
                assertEquals(59, result.getDictionary("main")!!.getInt("number2"))
                assertNull(result.getDictionary("secondary"))
            }
            if (n == 42) {
                assertEquals(58, result.getDictionary("main")!!.getInt("number2"))
                assertEquals(42, result.getDictionary("secondary")!!.getInt("theone"))
            }
        }
        assertEquals(101, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testResultSetAllResults() {
        val doc1a = MutableDocument("doc1")
        doc1a.setInt("answer", 42)
        doc1a.setString("a", "string")
        baseTestDb.save(doc1a)

        val query = QueryBuilder.select(SR_DOCID, SR_DELETED)
            .from(DataSource.database(baseTestDb))
            .where(Meta.id.equalTo(Expression.string("doc1")))

        query.execute().use { rs ->
            assertEquals(1, rs.allResults().size)
            assertEquals(0, rs.allResults().size)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testStringToMillis() {
        createDateDocs()
        val expectedJST = listOf<Number?>(
            null,
            499105260000L,
            499105290000L,
            499105290500L,
            499105290550L,
            499105290555L
        )

        val expectedPST = listOf<Number?>(
            null,
            499166460000L,
            499166490000L,
            499166490500L,
            499166490550L,
            499166490555L
        )

        val expectedUTC = listOf<Number?>(
            null,
            499137660000L,
            499137690000L,
            499137690500L,
            499137690550L,
            499137690555L
        )

        val offset = TimeZone.currentSystemDefault()
            .offsetAt(Instant.fromEpochMilliseconds(499132800000L))
            .totalSeconds * 1000
        Report.log("Local offset: %d", offset)
        val expectedLocal = mutableListOf<Number>()
        expectedLocal.add(499132800000L - offset)
        var first = true
        for (entry in expectedUTC) {
            if (first) {
                first = false
                continue
            }
            expectedLocal.add(entry as Long - offset)
        }

        val query = QueryBuilder.select(
            SelectResult.expression(Function.stringToMillis(Expression.property("local"))),
            SelectResult.expression(Function.stringToMillis(Expression.property("JST"))),
            SelectResult.expression(Function.stringToMillis(Expression.property("JST2"))),
            SelectResult.expression(Function.stringToMillis(Expression.property("PST"))),
            SelectResult.expression(Function.stringToMillis(Expression.property("PST2"))),
            SelectResult.expression(Function.stringToMillis(Expression.property("UTC")))
        )
            .from(DataSource.database(baseTestDb))
            .orderBy(Ordering.property("local").ascending())

        verifyQuery(query) { n, result ->
            assertEquals(expectedLocal.get(n - 1), result.getNumber(0))
            assertEquals(expectedJST[n - 1], result.getNumber(1))
            assertEquals(expectedJST[n - 1], result.getNumber(2))
            assertEquals(expectedPST[n - 1], result.getNumber(3))
            assertEquals(expectedPST[n - 1], result.getNumber(4))
            assertEquals(expectedUTC[n - 1], result.getNumber(5))
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testStringToUTC() {
        createDateDocs()

        val expectedLocal = listOf(
            localToUTC("yyyy-MM-dd", "1985-10-26"),
            localToUTC("yyyy-MM-dd HH:mm", "1985-10-26 01:21"),
            localToUTC("yyyy-MM-dd HH:mm:ss", "1985-10-26 01:21:30"),
            localToUTC("yyyy-MM-dd HH:mm:ss.SSS", "1985-10-26 01:21:30.500"),
            localToUTC("yyyy-MM-dd HH:mm:ss.SSS", "1985-10-26 01:21:30.550"),
            localToUTC("yyyy-MM-dd HH:mm:ss.SSS", "1985-10-26 01:21:30.555")
        )

        val expectedJST = listOf(
            null,
            "1985-10-25T16:21:00Z",
            "1985-10-25T16:21:30Z",
            "1985-10-25T16:21:30.500Z",
            "1985-10-25T16:21:30.550Z",
            "1985-10-25T16:21:30.555Z"
        )

        val expectedPST = listOf(
            null,
            "1985-10-26T09:21:00Z",
            "1985-10-26T09:21:30Z",
            "1985-10-26T09:21:30.500Z",
            "1985-10-26T09:21:30.550Z",
            "1985-10-26T09:21:30.555Z"
        )

        val expectedUTC = listOf(
            null,
            "1985-10-26T01:21:00Z",
            "1985-10-26T01:21:30Z",
            "1985-10-26T01:21:30.500Z",
            "1985-10-26T01:21:30.550Z",
            "1985-10-26T01:21:30.555Z"
        )

        val query = QueryBuilder.select(
            SelectResult.expression(Function.stringToUTC(Expression.property("local"))),
            SelectResult.expression(Function.stringToUTC(Expression.property("JST"))),
            SelectResult.expression(Function.stringToUTC(Expression.property("JST2"))),
            SelectResult.expression(Function.stringToUTC(Expression.property("PST"))),
            SelectResult.expression(Function.stringToUTC(Expression.property("PST2"))),
            SelectResult.expression(Function.stringToUTC(Expression.property("UTC")))
        )
            .from(DataSource.database(baseTestDb))
            .orderBy(Ordering.property("local").ascending())

        verifyQuery(query) { n, result ->
            assertEquals(expectedLocal[n - 1], result.getString(0))
            assertEquals(expectedJST[n - 1], result.getString(1))
            assertEquals(expectedJST[n - 1], result.getString(2))
            assertEquals(expectedPST[n - 1], result.getString(3))
            assertEquals(expectedPST[n - 1], result.getString(4))
            assertEquals(expectedUTC[n - 1], result.getString(5))
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testMillisConversion() {
        val millis = arrayOf<Number>(
            499132800000L,
            499137660000L,
            499137690000L,
            499137690500L,
            499137690550L,
            499137690555L
        )

        val expectedUTC = listOf(
            "1985-10-26T00:00:00Z",
            "1985-10-26T01:21:00Z",
            "1985-10-26T01:21:30Z",
            "1985-10-26T01:21:30.500Z",
            "1985-10-26T01:21:30.550Z",
            "1985-10-26T01:21:30.555Z"
        )

        //val expectedLocal = mutableListOf<String>()

        for (t in millis) {
            baseTestDb.save(MutableDocument().setNumber("timestamp", t))
        }

        val query = QueryBuilder.select(
            SelectResult.expression(Function.millisToString(Expression.property("timestamp"))),
            SelectResult.expression(Function.millisToUTC(Expression.property("timestamp")))
        )
            .from(DataSource.database(baseTestDb))
            .orderBy(Ordering.property("timestamp").ascending())

        verifyQuery(query) { n, result ->
            val i = n - 1
            assertEquals(expectedUTC[i], result.getString(1))
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testQueryDocumentWithDollarSign() {
        baseTestDb.save(
            MutableDocument("doc1")
                .setString("\$type", "book")
                .setString("\$description", "about cats")
                .setString("\$price", "$100")
        )
        baseTestDb.save(
            MutableDocument("doc2")
                .setString("\$type", "book")
                .setString("\$description", "about dogs")
                .setString("\$price", "$95")
        )
        baseTestDb.save(
            MutableDocument("doc3")
                .setString("\$type", "animal")
                .setString("\$description", "puppy")
                .setString("\$price", "$195")
        )

        var cheapBooks = 0
        var books = 0

        val q = QueryBuilder.select(
            SelectResult.expression(Meta.id),
            SelectResult.expression(Expression.property("\$type")),
            SelectResult.expression(Expression.property("\$price"))
        )
            .from(DataSource.database(baseTestDb))
            .where(Expression.property("\$type").equalTo(Expression.string("book")))

        q.execute().use { res ->
            for (r in res) {
                books++
                val p = r.getString("\$price")!!
                if (p.substring(1).toInt() < 100) {
                    cheapBooks++
                }
            }
            assertEquals(2, books)
            assertEquals(1, cheapBooks)
        }
    }

    // ??? This is a ridiculously expensive test
    @Throws(CouchbaseLiteException::class)
    private fun testLiveQueryNoUpdate(consumeAll: Boolean) = runBlocking {
        loadNumberedDocs(100)

        val query = QueryBuilder.select()
            .from(DataSource.database(baseTestDb))
            .where(EXPR_NUMBER1.lessThan(Expression.intValue(10)))
            .orderBy(Ordering.property("number1").ascending())

        val latch = CountDownLatch(2)
        val listener = { change: QueryChange ->
            if (consumeAll) {
                val rs = change.results
                @Suppress("ControlFlowWithEmptyBody")
                while (rs?.next() != null) {
                }
            }

            latch.countDown()
            // should happen only once!
        }

        val token = query.addChangeListener(listener)
        try {
            // create one doc
            executeAsync(500) {
                try {
                    createNumberedDocInBaseTestDb(111, 100)
                } catch (e: CouchbaseLiteException) {
                    throw RuntimeException(e)
                }
            }

            // Wait 5 seconds
            // The latch should not pop, because the listener should be called only once
            assertFailsWith<TimeoutCancellationException> {
                withTimeout(5.seconds) {
                    latch.await()
                }
            }
            assertEquals(1, latch.getCount())
        } finally {
            query.removeChangeListener(token)
        }
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testN1QLSelect() {
        loadNumberedDocs(100)

        val numRows = verifyQuery(
            baseTestDb.createQuery("SELECT number1, number2 FROM _default")
        ) { n, result ->
            assertEquals(n, result.getInt("number1"))
            assertEquals(n, result.getInt(0))
            assertEquals(100 - n, result.getInt("number2"))
            assertEquals(100 - n, result.getInt(1))
        }

        assertEquals(100, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testN1QLSelectStarFromDefault() {
        loadNumberedDocs(100)

        val numRows = verifyQuery(
            baseTestDb.createQuery("SELECT * FROM _default")
        ) { n, result ->
            assertEquals(1, result.count)
            val a1 = result.getDictionary(0)
            val a2 = result.getDictionary("_default")
            assertEquals(n, a1!!.getInt("number1"))
            assertEquals((100 - n), a1.getInt("number2"))
            assertEquals(n, a2!!.getInt("number1"))
            assertEquals((100 - n), a2.getInt("number2"))
        }

        assertEquals(100, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testN1QLSelectStarFromDatabase() {
        loadNumberedDocs(100)
        val dbName = baseTestDb.name

        val numRows = verifyQuery(
            baseTestDb.createQuery("SELECT * FROM $dbName")
        ) { n, result ->
            assertEquals(1, result.count)
            val a1 = result.getDictionary(0)
            val a2 = result.getDictionary(dbName)
            assertEquals(n, a1!!.getInt("number1"))
            assertEquals((100 - n), a1.getInt("number2"))
            assertEquals(n, a2!!.getInt("number1"))
            assertEquals((100 - n), a2.getInt("number2"))
        }

        assertEquals(100, numRows)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testN1QLSelectStarFromUnderscore() {
        loadNumberedDocs(100)

        val numRows = verifyQuery(
            baseTestDb.createQuery("SELECT * FROM _")
        ) { n, result ->
            assertEquals(1, result.count)
            val a1 = result.getDictionary(0)
            val a2 = result.getDictionary("_")
            assertEquals(n, a1!!.getInt("number1"))
            assertEquals((100 - n), a1.getInt("number2"))
            assertEquals(n, a2!!.getInt("number1"))
            assertEquals((100 - n), a2.getInt("number2"))
        }

        assertEquals(100, numRows)
    }

    @Throws(CouchbaseLiteException::class)
    private fun runTestCases(vararg cases: TestCase) {
        for (testCase in cases) {
            val docIdList = testCase.docIds.toMutableList()

            val numRows = verifyQuery(
                QueryBuilder.select(SR_DOCID).from(DataSource.database(baseTestDb))
                    .where(testCase.expr)
            ) { _, result ->
                val docID = result.getString(0)
                docIdList.remove(docID)
            }
            assertEquals(0, docIdList.size)
            assertEquals(testCase.docIds.size, numRows)
        }
    }

    @Throws(CouchbaseLiteException::class)
    private fun createAlphaDocs() {
        val letters = arrayOf("B", "Z", "Å", "A")
        for (letter in letters) {
            val doc = MutableDocument()
            doc.setValue("string", letter)
            saveDocInBaseTestDb(doc)
        }
    }

    @Throws(CouchbaseLiteException::class)
    private fun createDateDocs() {
        var doc = MutableDocument()
        doc.setString("local", "1985-10-26")
        baseTestDb.save(doc)

        val dateTimeFormats = listOf(
            "1985-10-26 01:21",
            "1985-10-26 01:21:30",
            "1985-10-26 01:21:30.5",
            "1985-10-26 01:21:30.55",
            "1985-10-26 01:21:30.555"
        )

        for (format in dateTimeFormats) {
            doc = MutableDocument()
            doc.setString("local", format)
            doc.setString("JST", "$format+09:00")
            doc.setString("JST2", "$format+0900")
            doc.setString("PST", "$format-08:00")
            doc.setString("PST2", "$format-0800")
            doc.setString("UTC", format + "Z")
            baseTestDb.save(doc)
        }
    }

    @Throws(CouchbaseLiteException::class)
    private fun createTaskDocument(title: String, complete: Boolean): Document {
        val doc = MutableDocument()
        doc.setString("type", "task")
        doc.setString("title", title)
        doc.setBoolean("complete", complete)
        return saveDocInBaseTestDb(doc)
    }

    @Throws(CouchbaseLiteException::class)
    private fun testOrdered(ordering: Ordering, cmp: Comparator<String>) {
        val firstNames = mutableListOf<String>()
        val numRows = verifyQuery(
            QueryBuilder.select(SR_DOCID).from(DataSource.database(baseTestDb)).orderBy(ordering),
            false
        ) { _, result ->
            val docID = result.getString(0)!!
            val doc = baseTestDb.getDocument(docID)!!
            val name = doc.getDictionary("name")!!.toMap()
            val firstName = name["first"] as String
            firstNames.add(firstName)
        }
        assertEquals(100, numRows)
        assertEquals(100, firstNames.size)

        val sorted = firstNames.toMutableList()
        sorted.sortedWith(cmp)
        assertContentEquals(sorted, firstNames)
    }

    companion object {
        private val EXPR_NUMBER1 = Expression.property("number1")
        private val EXPR_NUMBER2 = Expression.property("number2")

        private val SR_DOCID = SelectResult.expression(Meta.id)
        private val SR_REVID = SelectResult.expression(Meta.revisionID)
        private val SR_SEQUENCE = SelectResult.expression(Meta.sequence)
        private val SR_DELETED = SelectResult.expression(Meta.deleted)
        private val SR_EXPIRATION = SelectResult.expression(Meta.expiration)
        private val SR_ALL = SelectResult.all()
        private val SR_NUMBER1 = SelectResult.property("number1")
    }
}

expect fun localToUTC(format: String, dateStr: String): String
