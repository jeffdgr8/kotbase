/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("LocalVariableName")

package kotbase

import co.touchlab.stately.collections.ConcurrentMutableList
import com.couchbase.lite.asJSON
import kotbase.internal.utils.Report
import kotbase.internal.utils.paddedString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.CountDownLatch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement
import kotlin.math.*
import kotlin.random.Random
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalAtomicApi::class)
class QueryTest : BaseQueryTest() {

    private class MathFn(
        val name: String,
        val expr: Expression,
        val expected: Double
    )

    private class TestCase(
        val expr: Expression,
        val docIds: List<String>
    ) {
        constructor(expr: Expression, vararg documentIDs: String) : this(expr, documentIDs.asList())

        constructor(expr: Expression, docIds: List<String>, vararg pos: Int) : this(
            expr,
            docIds.filterIndexed { index, _ -> index + 1 in pos }
        )
    }

    @Test
    fun testQueryGetColumnNameAfter32Items() {
        val value = getUniqueName("value")

        val document = MutableDocument()
        document.setString(TEST_DOC_TAG_KEY, value)
        saveDocInCollection(document)

        val queryBuild = QueryBuilder.createQuery("""
            select
              `1`,`2`,`3`,`4`,`5`,`6`,`7`,`8`,`9`,`10`,`11`,`12`, `13`,`14`,`15`,`16`,`17`,`18`,`19`,`20`,
              `21`,`22`,`23`,`24`,`25`,`26`,`27`,`28`,`29`,`30`,`31`,`32`,`key`
              from _ 
             limit 1
        """.trimIndent(),
            testDatabase
        )

        val res = arrayOfNulls<String>(33)
        res[32] = value
        val arrayResult = res.asList()

        val mapResult = mapOf(
            TEST_DOC_TAG_KEY to "value"
        )

        queryBuild.execute().use { rs ->
            while (true) {
                val result = rs.next() ?: break
                assertEquals("{\"key\":\"value\"}", result.toJSON())
                assertEquals(arrayResult, result.toList())
                assertEquals(mapResult, result.toMap())
                assertEquals("value", result.getValue(TEST_DOC_TAG_KEY).toString())
                assertEquals("value", result.getString(TEST_DOC_TAG_KEY))
                assertEquals("value", result.getString(32))
            }
        }
    }

    @Test
    fun testQueryDocumentExpiration() = runBlocking {
        val now = Clock.System.now()

        // this one should expire
        val doc = MutableDocument()
        doc.setInt("answer", 42)
        doc.setString("notHere", "string")
        saveDocInCollection(doc)
        testCollection.setDocumentExpiration(doc.id, now + 500.milliseconds)

        // this one is deleted
        val doc10 = MutableDocument()
        doc10.setInt("answer", 42)
        doc10.setString("notHere", "string")
        saveDocInCollection(doc10)
        testCollection.setDocumentExpiration(doc10.id, now + 2000.milliseconds) //deleted doc
        testCollection.delete(doc10)

        // should be in the result set
        val doc1 = MutableDocument()
        doc1.setInt("answer", 42)
        doc1.setString("a", "string")
        saveDocInCollection(doc1)
        testCollection.setDocumentExpiration(doc1.id, now + 2000.milliseconds)

        // should be in the result set
        val doc2 = MutableDocument()
        doc2.setInt("answer", 42)
        doc2.setString("b", "string")
        saveDocInCollection(doc2)
        testCollection.setDocumentExpiration(doc2.id, now + 3000.milliseconds)

        // should be in the result set
        val doc3 = MutableDocument()
        doc3.setInt("answer", 42)
        doc3.setString("c", "string")
        saveDocInCollection(doc3)
        testCollection.setDocumentExpiration(doc3.id, now + 4000.milliseconds)

        delay(1000)

        // This should get all but the one that has expired
        // and the one that was deleted
        val query = QueryBuilder.select(SelectResult.expression(Meta.id), SelectResult.expression(Meta.expiration))
            .from(DataSource.collection(testCollection))
            .where(Meta.expiration.lessThan(Expression.longValue(now.toEpochMilliseconds() + 6000L)))

        assertEquals(3, verifyQueryWithEnumerator(query) { _, _ -> })
    }

    @Test
    fun testQueryDocumentIsNotDeleted() {
        val doc1a = MutableDocument()
        doc1a.setInt("answer", 42)
        doc1a.setString("a", "string")
        saveDocInCollection(doc1a)

        val query = QueryBuilder.select(SelectResult.expression(Meta.id), SelectResult.expression(Meta.deleted))
            .from(DataSource.collection(testCollection))
            .where(Meta.id.equalTo(Expression.string(doc1a.id))
                .and(Meta.deleted.equalTo(Expression.booleanValue(false)))
            )

        assertEquals(
            1,
            verifyQueryWithEnumerator(query) { _, result ->
                assertEquals(result.getString(0), doc1a.id)
                assertFalse(result.getBoolean(1))
            }
        )
    }

    @Test
    fun testQueryDocumentIsDeleted() {
        val doc = MutableDocument()
        doc.setInt("answer", 42)
        doc.setString("a", "string")
        saveDocInCollection(doc)

        testCollection.delete(testCollection.getDocument(doc.id)!!)

        val query = QueryBuilder.select(SelectResult.expression(Meta.id), SelectResult.expression(Meta.deleted))
            .from(DataSource.collection(testCollection))
            .where(Meta.deleted.equalTo(Expression.booleanValue(true))
                .and(Meta.id.equalTo(Expression.string(doc.id)))
            )

        assertEquals(1, verifyQueryWithEnumerator(query) { _, _ -> })
    }

    @Test
    fun testNoWhereQuery() {
        loadJSONResourceIntoCollection("names_100.json")

        verifyQuery(
            QueryBuilder.select(SelectResult.expression(Meta.id), SelectResult.expression(Meta.sequence))
                .from(DataSource.collection(testCollection)),
            100
        ) { n, result ->
            val docID = result.getString(0)
            val expectedID: String = jsonDocId(n)
            val sequence = result.getInt(1)

            assertEquals(expectedID, docID)

            assertEquals(n, sequence)

            val doc = testCollection.getDocument(docID!!)!!
            assertEquals(expectedID, doc.id)
            assertEquals(n.toLong(), doc.sequence)
        }
    }

    @Test
    fun testWhereComparison() {
        val docIds = loadDocuments(10).map(Document::id)
        runTests(
            TestCase(Expression.property(TEST_DOC_SORT_KEY).lessThan(Expression.intValue(3)), docIds, 1, 2),
            TestCase(
                Expression.property(TEST_DOC_SORT_KEY).greaterThanOrEqualTo(Expression.intValue(3)),
                docIds,
                3, 4, 5, 6, 7, 8, 9, 10
            ),
            TestCase(
                Expression.property(TEST_DOC_SORT_KEY).lessThanOrEqualTo(Expression.intValue(3)),
                docIds,
                1, 2, 3
            ),
            TestCase(
                Expression.property(TEST_DOC_SORT_KEY).greaterThan(Expression.intValue(3)),
                docIds,
                4, 5, 6, 7, 8, 9, 10
            ),
            TestCase(
                Expression.property(TEST_DOC_SORT_KEY).greaterThan(Expression.intValue(6)),
                docIds,
                7, 8, 9, 10
            ),
            TestCase(
                Expression.property(TEST_DOC_SORT_KEY).lessThanOrEqualTo(Expression.intValue(6)),
                docIds,
                1, 2, 3, 4, 5, 6
            ),
            TestCase(
                Expression.property(TEST_DOC_SORT_KEY).greaterThanOrEqualTo(Expression.intValue(6)),
                docIds,
                6, 7, 8, 9, 10
            ),
            TestCase(
                Expression.property(TEST_DOC_SORT_KEY).lessThan(Expression.intValue(6)),
                docIds,
                1, 2, 3, 4, 5
            ),
            TestCase(Expression.property(TEST_DOC_SORT_KEY).equalTo(Expression.intValue(7)), docIds, 7),
            TestCase(
                Expression.property(TEST_DOC_SORT_KEY).notEqualTo(Expression.intValue(7)),
                docIds,
                1, 2, 3, 4, 5, 6, 8, 9, 10
            )
        )
    }


    @Test
    fun testWhereArithmetic() {
        val docIds = loadDocuments(10).map(Document::id)
        runTests(
            TestCase(
                Expression.property(TEST_DOC_SORT_KEY).multiply(Expression.intValue(2))
                    .greaterThan(Expression.intValue(3)),
                docIds,
                2, 3, 4, 5, 6, 7, 8, 9, 10
            ),
            TestCase(
                Expression.property(TEST_DOC_SORT_KEY).divide(Expression.intValue(2))
                    .greaterThan(Expression.intValue(3)),
                docIds,
                8, 9, 10
            ),
            TestCase(
                Expression.property(TEST_DOC_SORT_KEY).modulo(Expression.intValue(2)).equalTo(Expression.intValue(0)),
                docIds,
                2, 4, 6, 8, 10
            ),
            TestCase(
                Expression.property(TEST_DOC_SORT_KEY).add(Expression.intValue(5)).greaterThan(Expression.intValue(10)),
                docIds,
                6, 7, 8, 9, 10
            ),
            TestCase(
                Expression.property(TEST_DOC_SORT_KEY).subtract(Expression.intValue(5))
                    .greaterThan(Expression.intValue(0)),
                docIds,
                6, 7, 8, 9, 10
            ),
            TestCase(
                Expression.property(TEST_DOC_SORT_KEY).multiply(Expression.property(TEST_DOC_REV_SORT_KEY))
                    .greaterThan(Expression.intValue(10)),
                docIds,
                2, 3, 4, 5, 6, 7, 8
            ),
            TestCase(
                Expression.property(TEST_DOC_REV_SORT_KEY).divide(Expression.property(TEST_DOC_SORT_KEY))
                    .greaterThan(Expression.intValue(3)),
                docIds,
                1, 2
            ),
            TestCase(
                Expression.property(TEST_DOC_REV_SORT_KEY).modulo(Expression.property(TEST_DOC_SORT_KEY))
                    .equalTo(Expression.intValue(0)),
                docIds,
                1, 2, 5, 10
            ),
            TestCase(
                Expression.property(TEST_DOC_SORT_KEY).add(Expression.property(TEST_DOC_REV_SORT_KEY))
                    .equalTo(Expression.intValue(10)),
                docIds,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10
            ),
            TestCase(
                Expression.property(TEST_DOC_SORT_KEY).subtract(Expression.property(TEST_DOC_REV_SORT_KEY))
                    .greaterThan(Expression.intValue(0)),
                docIds,
                6, 7, 8, 9, 10
            )
        )
    }

    @Test
    fun testWhereAndOr() {
        val docIds = loadDocuments(10).map(Document::id)
        runTests(
            TestCase(
                Expression.property(TEST_DOC_SORT_KEY).greaterThan(Expression.intValue(3))
                    .and(Expression.property(TEST_DOC_REV_SORT_KEY).greaterThan(Expression.intValue(3))),
                docIds,
                4, 5, 6
            ),
            TestCase(
                Expression.property(TEST_DOC_SORT_KEY).lessThan(Expression.intValue(3))
                    .or(Expression.property(TEST_DOC_REV_SORT_KEY).lessThan(Expression.intValue(3))),
                docIds,
                1, 2, 8, 9, 10
            )
        )
    }

    @Test
    fun testWhereValued() {
        val doc1 = MutableDocument()
        doc1.setValue("name", "Scott")
        doc1.setValue("address", null)
        saveDocInCollection(doc1)

        val doc2 = MutableDocument()
        doc2.setValue("name", "Tiger")
        doc2.setValue("address", "123 1st ave.")
        doc2.setValue("age", 20)
        saveDocInCollection(doc2)

        val name = Expression.property("name")
        val address = Expression.property("address")
        val age = Expression.property("age")
        val work = Expression.property("work")

        for (testCase in arrayOf(
            TestCase(name.isNotValued()),
            TestCase(name.isValued(), doc1.id, doc2.id),
            TestCase(address.isNotValued(), doc1.id),
            TestCase(address.isValued(), doc2.id),
            TestCase(age.isNotValued(), doc1.id),
            TestCase(age.isValued(), doc2.id),
            TestCase(work.isNotValued(), doc1.id, doc2.id),
            TestCase(work.isValued())
        )) {
            val nIds = testCase.docIds.size
            verifyQuery(
                QueryBuilder.select(SelectResult.expression(Meta.id))
                    .from(DataSource.collection(testCollection))
                    .where(testCase.expr),
                nIds
            ) { n, result ->
                if (n <= nIds) {
                    assertEquals(
                        testCase.docIds[n - 1],
                        result.getString(0)
                    )
                }
            }
        }
    }

    @Test
    fun testWhereIs() {
        val doc1 = MutableDocument()
        doc1.setValue("string", "string")
        saveDocInCollection(doc1)

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))
            .where(Expression.property("string").`is`(Expression.string("string")))

        verifyQuery(
            query,
            1
        ) { _, result ->
            val docID = result.getString(0)
            assertEquals(doc1.id, docID)
            val doc = testCollection.getDocument(docID!!)!!
            assertEquals(doc1.getValue("string"), doc.getValue("string"))
        }
    }

    @Test
    fun testWhereIsNot() {
        val doc1 = MutableDocument()
        doc1.setValue("string", "string")
        saveDocInCollection(doc1)

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))
            .where(Expression.property("string").isNot(Expression.string("string1")))

        verifyQuery(
            query,
            1
        ) { _, result ->
            val docID = result.getString(0)
            assertEquals(doc1.id, docID)
            val doc = testCollection.getDocument(docID!!)!!
            assertEquals(doc1.getValue("string"), doc.getValue("string"))
        }
    }

    @Test
    fun testWhereBetween() {
        val docIds = loadDocuments(10).map(Document::id)
        runTests(TestCase(Expression.property(TEST_DOC_SORT_KEY)
            .between(Expression.intValue(3), Expression.intValue(7)), docIds, 3, 4, 5, 6, 7)
        )
    }

    @Test
    fun testWhereIn() {
        loadJSONResourceIntoCollection("names_100.json")

        val expected = arrayOf(
            Expression.string("Marcy"),
            Expression.string("Margaretta"),
            Expression.string("Margrett"),
            Expression.string("Marlen"),
            Expression.string("Maryjo")
        )

        val query = QueryBuilder.select(SelectResult.property("name.first"))
            .from(DataSource.collection(testCollection))
            .where(Expression.property("name.first").`in`(*expected))
            .orderBy(Ordering.property("name.first"))

        verifyQuery(query, 5) { n, result -> assertEquals(expected[n - 1].asJSON(), result.getString(0)) }
    }

    @Test
    fun testWhereLike() {
        loadJSONResourceIntoCollection("names_100.json")

        val w = Expression.property("name.first").like(Expression.string("%Mar%"))
        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))
            .where(w)
            .orderBy(Ordering.property("name.first").ascending())

        val firstNames = mutableListOf<String>()
        assertEquals(
            5,
            verifyQueryWithEnumerator(
                query
            ) { _, result ->
                val docID = result.getString(0)
                val doc = testCollection.getDocument(docID!!)!!
                val name = doc.getDictionary("name")!!.toMap()
                val firstName = name["first"] as String?
                if (firstName != null) { firstNames.add(firstName) }
            }
        )
        assertEquals(5, firstNames.size)
    }

    @Test
    fun testWhereRegex() {
        loadJSONResourceIntoCollection("names_100.json")

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))
            .where(Expression.property("name.first").regex(Expression.string("^Mar.*")))
            .orderBy(Ordering.property("name.first").ascending())

        val firstNames = mutableListOf<String>()
        assertEquals(
            5,
            verifyQueryWithEnumerator(
                query
            ) { _, result ->
                val docID = result.getString(0)
                val doc = testCollection.getDocument(docID!!)!!
                val name = doc.getDictionary("name")!!.toMap()
                val firstName = name["first"] as String?
                if (firstName != null) { firstNames.add(firstName) }
            }
        )
        assertEquals(5, firstNames.size)
    }

    @Test
    fun testRank() {
        val expr = FullTextFunction.rank(Expression.fullTextIndex("abc"))
        assertNotNull(expr)
        val obj = expr.asJSON()
        assertNotNull(obj)
        assertIs<List<*>>(obj)
        assertEquals(listOf("RANK()", "abc"), obj)
    }

    @Test
    fun testWhereIndexMatch() {
        loadJSONResourceIntoCollection("sentences.json")

        testCollection.createIndex("sentence", IndexBuilder.fullTextIndex(FullTextIndexItem.property("sentence")))
        val idx = Expression.fullTextIndex("sentence")

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.property("sentence"))
            .from(DataSource.collection(testCollection))
            .where(FullTextFunction.match(idx, "'Dummie woman'"))
            .orderBy(Ordering.expression(FullTextFunction.rank(idx)).descending())

        verifyQuery(
            query,
            2
        ) { _, result ->
            assertNotNull(result.getString(0))
            assertNotNull(result.getString(1))
        }
    }

    @Test
    fun testWhereMatch() {
        loadJSONResourceIntoCollection("sentences.json")

        testCollection.createIndex("sentence", IndexBuilder.fullTextIndex(FullTextIndexItem.property("sentence")))
        val idx = Expression.fullTextIndex("sentence")

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.property("sentence"))
            .from(DataSource.collection(testCollection))
            .where(FullTextFunction.match(idx, "'Dummie woman'"))
            .orderBy(Ordering.expression(FullTextFunction.rank(idx)).descending())

        verifyQuery(
            query,
            2
        ) { _, result ->
            assertNotNull(result.getString(0))
            assertNotNull(result.getString(1))
        }
    }

    @Test
    fun testFullTextIndexConfigDefaults() {
        val idxConfig = FullTextIndexConfiguration("sentence", "nonsense")
        assertEquals(Defaults.FullTextIndex.IGNORE_ACCENTS, idxConfig.isIgnoringAccents)
        assertEquals("en", idxConfig.language)

        idxConfig.setLanguage(null)
        assertNull(idxConfig.language)
    }

    @Test
    fun testFullTextIndexConfig() {
        loadJSONResourceIntoCollection("sentences.json")

        val idxConfig = FullTextIndexConfiguration("sentence", "nonsense")
            .setLanguage("en-ca")
            .ignoreAccents(true)
        assertEquals("en-ca", idxConfig.language)
        assertTrue(idxConfig.isIgnoringAccents)

        testCollection.createIndex("sentence", idxConfig)
        val idx = Expression.fullTextIndex("sentence")

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.property("sentence"))
            .from(DataSource.collection(testCollection))
            .where(FullTextFunction.match(idx, "'Dummie woman'"))
            .orderBy(Ordering.expression(FullTextFunction.rank(idx)).descending())

        verifyQuery(
            query,
            2
        ) { _, result ->
            assertNotNull(result.getString(0))
            assertNotNull(result.getString(1))
        }
    }

    // Test courtesy of Jayahari Vavachan
    @Test
    fun testN1QLFTSQuery() {
        loadJSONResourceIntoCollection("sentences.json")

        testCollection.createIndex("sentence", IndexBuilder.fullTextIndex(FullTextIndexItem.property("sentence")))

        val query = testDatabase.createQuery(
            "SELECT _id FROM " + testCollection.fullName
                    + " WHERE MATCH(sentence, 'Dummie woman')"
        )

        verifyQuery(query, 2) { _, result -> assertNotNull(result.getString(0)) }
    }

    @Test
    fun testOrderBy() {
        loadJSONResourceIntoCollection("names_100.json")

        val order = Ordering.expression(Expression.property("name.first"))

        // Don't replace this with Comparator.naturalOrder.
        // it doesn't exist on older versions of Android
        testOrdered(order.ascending(), String::compareTo)
        testOrdered(order.descending()) { c1, c2 -> c2.compareTo(c1) }
    }

    // https://github.com/couchbase/couchbase-lite-ios/issues/1669
    // https://github.com/couchbase/couchbase-lite-core/issues/81
    @Test
    fun testSelectDistinct() {
        val doc1 = MutableDocument()
        doc1.setValue("number", 20)
        saveDocInCollection(doc1)

        val doc2 = MutableDocument()
        doc2.setValue("number", 20)
        saveDocInCollection(doc2)

        verifyQuery(
            QueryBuilder.selectDistinct(SelectResult.property("number"))
                .from(DataSource.collection(testCollection)),
            1
        ) { _, result -> assertEquals(20, result.getInt(0)) }
    }

    @Test
    fun testJoin() {
        loadDocuments(100)

        val doc1 = MutableDocument()
        doc1.setValue("theone", 42)
        saveDocInCollection(doc1)

        val join = Join.join(DataSource.collection(testCollection).`as`("secondary"))
            .on(Expression.property(TEST_DOC_SORT_KEY).from("main")
                .equalTo(Expression.property("theone").from("secondary"))
            )

        val query = QueryBuilder.select(SelectResult.expression(Meta.id.from("main")))
            .from(DataSource.collection(testCollection).`as`("main"))
            .join(join)

        verifyQuery(
            query,
            1
        ) { _, result ->
            val docID = result.getString(0)
            val doc = testCollection.getDocument((docID)!!)!!
            assertEquals(42, doc.getInt(TEST_DOC_SORT_KEY))
        }
    }

    @Test
    fun testLeftJoin() {
        loadDocuments(100)

        val joinme = MutableDocument()
        joinme.setValue("theone", 42)
        saveDocInCollection(joinme)

        val query = QueryBuilder.select(
            SelectResult.expression(Expression.property(TEST_DOC_REV_SORT_KEY).from("main")),
            SelectResult.expression(Expression.property("theone").from("secondary"))
        )
            .from(DataSource.collection(testCollection).`as`("main"))
            .join(Join.leftJoin(DataSource.collection(testCollection).`as`("secondary"))
                .on(Expression.property(TEST_DOC_SORT_KEY).from("main")
                    .equalTo(Expression.property("theone").from("secondary"))
                )
            )

        verifyQuery(
            query,
            101
        ) { n, result ->
            if (n == 41) {
                assertEquals(59, result.getInt(0))
                assertNull(result.getValue(1))
            }
            if (n == 42) {
                assertEquals(58, result.getInt(0))
                assertEquals(42, result.getInt(1))
            }
        }
    }

    @Test
    fun testCrossJoin() {
        loadDocuments(10)

        val query = QueryBuilder.select(
            SelectResult.expression(Expression.property(TEST_DOC_SORT_KEY).from("main")),
            SelectResult.expression(Expression.property(TEST_DOC_REV_SORT_KEY).from("secondary"))
        )
            .from(DataSource.collection(testCollection).`as`("main"))
            .join(Join.crossJoin(DataSource.collection(testCollection).`as`("secondary")))

        verifyQuery(
            query,
            100
        ) { n, result ->
            val num1 = result.getInt(0)
            val num2 = result.getInt(1)
            assertEquals((num1 - 1) % 10, (n - 1) / 10)
            assertEquals((10 - num2) % 10, n % 10)
        }
    }

    @Test
    fun testGroupBy() {
        loadJSONResourceIntoCollection("names_100.json")

        val expectedStates = listOf("AL", "CA", "CO", "FL", "IA")
        val expectedCounts = listOf(1, 6, 1, 1, 3)
        val expectedMaxZips = listOf("35243", "94153", "81223", "33612", "50801")

        val state = Expression.property("contact.address.state")
        var query = QueryBuilder
            .select(
                SelectResult.property("contact.address.state"),
                SelectResult.expression(Function.count(Expression.intValue(1))),
                SelectResult.expression(Function.max(Expression.property("contact.address.zip")))
            )
            .from(DataSource.collection(testCollection))
            .where(Expression.property("gender").equalTo(Expression.string("female")))
            .groupBy(state)
            .orderBy(Ordering.expression(state))

        verifyQuery(
            query,
            31
        ) { n, result ->
            val state1 = result.getValue(0) as String?
            val count1 = result.getValue(1) as Long
            val maxZip1 = result.getValue(2) as String?
            if (n - 1 < expectedStates.size) {
                assertEquals(expectedStates[n - 1], state1)
                assertEquals(expectedCounts[n - 1].toLong(), count1)
                assertEquals(expectedMaxZips[n - 1], maxZip1)
            }
        }

        // With HAVING:
        val expectedStates2 = listOf("CA", "IA", "IN")
        val expectedCounts2 = listOf(6, 3, 2)
        val expectedMaxZips2 = listOf("94153", "50801", "47952")

        query = QueryBuilder
            .select(
                SelectResult.property("contact.address.state"),
                SelectResult.expression(Function.count(Expression.intValue(1))),
                SelectResult.expression(Function.max(Expression.property("contact.address.zip")))
            )
            .from(DataSource.collection(testCollection))
            .where(Expression.property("gender").equalTo(Expression.string("female")))
            .groupBy(state)
            .having(Function.count(Expression.intValue(1)).greaterThan(Expression.intValue(1)))
            .orderBy(Ordering.expression(state))

        verifyQuery(
            query,
            15
        ) { n, result ->
            val state12 = result.getValue(0) as String?
            val count12 = result.getValue(1) as Long
            val maxZip12 = result.getValue(2) as String?
            if (n - 1 < expectedStates2.size) {
                assertEquals(expectedStates2[n - 1], state12)
                assertEquals(expectedCounts2[n - 1].toLong(), count12)
                assertEquals(expectedMaxZips2[n - 1], maxZip12)
            }
        }
    }

    @Test
    fun testParameters() {
        loadDocuments(100)

        val query = QueryBuilder
            .select(SelectResult.property(TEST_DOC_SORT_KEY))
            .from(DataSource.collection(testCollection))
            .where(Expression.property(TEST_DOC_SORT_KEY)
                .between(Expression.parameter("num1"), Expression.parameter("num2"))
            )
            .orderBy(Ordering.expression(Expression.property(TEST_DOC_SORT_KEY)))

        val params = Parameters(query.parameters)
            .setValue("num1", 2)
            .setValue("num2", 5)
        query.parameters = params

        val expectedNumbers = longArrayOf(2, 3, 4, 5)
        verifyQuery(query, 4) { n, result -> assertEquals(expectedNumbers[n - 1], result.getValue(0) as Long) }
    }

    @Test
    fun testMeta() {
        val expected = loadDocuments(5).map(Document::id)

        val query = QueryBuilder
            .select(
                SelectResult.expression(Meta.id),
                SelectResult.expression(Meta.sequence),
                SelectResult.expression(Meta.revisionID),
                SelectResult.property(TEST_DOC_SORT_KEY)
            )
            .from(DataSource.collection(testCollection))
            .orderBy(Ordering.expression(Meta.sequence))

        verifyQuery(
            query,
            5
        ) { n, result ->
            val docID1 = result.getValue(0) as String?
            val docID2 = result.getString(0)
            val docID3 = result.getValue("id") as String?
            val docID4 = result.getString("id")

            val seq1 = result.getValue(1) as Long
            val seq2 = result.getLong(1)
            val seq3 = result.getValue("sequence") as Long
            val seq4 = result.getLong("sequence")

            val revId1 = result.getValue(2) as String?
            val revId2 = result.getString(2)
            val revId3 = result.getValue("revisionID") as String?
            val revId4 = result.getString("revisionID")

            val number = result.getValue(3) as Long

            assertEquals(docID1, docID2)
            assertEquals(docID2, docID3)
            assertEquals(docID3, docID4)
            assertEquals(docID4, expected[n - 1])

            assertEquals(n.toLong(), seq1)
            assertEquals(n.toLong(), seq2)
            assertEquals(n.toLong(), seq3)
            assertEquals(n.toLong(), seq4)

            assertEquals(revId1, revId2)
            assertEquals(revId2, revId3)
            assertEquals(revId3, revId4)
            assertEquals(revId4, testCollection.getDocument(docID1!!)!!.revisionID)

            assertEquals(n.toLong(), number)
        }
    }

    @Test
    fun testRevisionIdInCreate() {
        val doc = MutableDocument()
        saveDocInCollection(doc)

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.revisionID))
            .from(DataSource.collection(testCollection))
            .where(Meta.id.equalTo(Expression.string(doc.id)))

        verifyQuery(query, 1) { _, result -> assertEquals(doc.revisionID, result.getString(0)) }
    }

    @Test
    fun testRevisionIdInUpdate() {
        var doc = MutableDocument()
        saveDocInCollection(doc)

        doc = testCollection.getDocument(doc.id)!!.toMutable()
        doc.setString("DEC", "Maynard")
        saveDocInCollection(doc)
        val revId = doc.revisionID

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.revisionID))
            .from(DataSource.collection(testCollection))
            .where(Meta.id.equalTo(Expression.string(doc.id)))

        verifyQuery(query, 1) { _, result -> assertEquals(revId, result.getString(0)) }
    }

    @Test
    fun testRevisionIdInWhere() {
        val doc = MutableDocument()
        saveDocInCollection(doc)

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))
            .where(Meta.revisionID.equalTo(Expression.string(doc.revisionID)))

        verifyQuery(query, 1) { _, result -> assertEquals(doc.id, result.getString(0)) }
    }

    @Test
    fun testRevisionIdInDelete() {
        val doc = MutableDocument()
        saveDocInCollection(doc)

        val dbDoc = testCollection.getDocument(doc.id)
        assertNotNull(dbDoc)

        testCollection.delete(dbDoc)

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.revisionID))
            .from(DataSource.collection(testCollection))
            .where(Meta.deleted.equalTo(Expression.booleanValue(true)))

        verifyQuery(query, 1) { _, result -> assertEquals(dbDoc.revisionID, result.getString(0)) }
    }

    @Test
    fun testLimit() {
        loadDocuments(10)

        var query = QueryBuilder
            .select(SelectResult.property(TEST_DOC_SORT_KEY))
            .from(DataSource.collection(testCollection))
            .orderBy(Ordering.expression(Expression.property(TEST_DOC_SORT_KEY)))
            .limit(Expression.intValue(5))

        val expectedNumbers = longArrayOf(1, 2, 3, 4, 5)
        verifyQuery(
            query,
            5
        ) { n, result ->
            val number = result.getValue(0) as Long
            assertEquals(expectedNumbers[n - 1], number)
        }

        val paramExpr = Expression.parameter("LIMIT_NUM")
        query = QueryBuilder
            .select(SelectResult.property(TEST_DOC_SORT_KEY))
            .from(DataSource.collection(testCollection))
            .orderBy(Ordering.expression(Expression.property(TEST_DOC_SORT_KEY)))
            .limit(paramExpr)
        val params = Parameters(query.parameters).setValue("LIMIT_NUM", 3)
        query.parameters = params

        val expectedNumbers2 = longArrayOf(1, 2, 3)
        verifyQuery(
            query,
            3
        ) { n, result ->
            val number = result.getValue(0) as Long
            assertEquals(expectedNumbers2[n - 1], number)
        }
    }

    @Test
    fun testLimitOffset() {
        loadDocuments(10)

        var query = QueryBuilder
            .select(SelectResult.property(TEST_DOC_SORT_KEY))
            .from(DataSource.collection(testCollection))
            .orderBy(Ordering.expression(Expression.property(TEST_DOC_SORT_KEY)))
            .limit(Expression.intValue(5), Expression.intValue(3))

        val expectedNumbers = longArrayOf(4, 5, 6, 7, 8)
        verifyQuery(
            query,
            5
        ) { n, result -> assertEquals(expectedNumbers[n - 1], result.getValue(0) as Long) }

        val paramLimitExpr = Expression.parameter("LIMIT_NUM")
        val paramOffsetExpr = Expression.parameter("OFFSET_NUM")
        query = QueryBuilder
            .select(SelectResult.property(TEST_DOC_SORT_KEY))
            .from(DataSource.collection(testCollection))
            .orderBy(Ordering.expression(Expression.property(TEST_DOC_SORT_KEY)))
            .limit(paramLimitExpr, paramOffsetExpr)
        val params = Parameters(query.parameters)
            .setValue("LIMIT_NUM", 3)
            .setValue("OFFSET_NUM", 5)
        query.parameters = params

        val expectedNumbers2 = longArrayOf(6, 7, 8)
        verifyQuery(
            query,
            3
        ) { n, result -> assertEquals(expectedNumbers2[n - 1], result.getValue(0) as Long) }
    }

    @Test
    fun testQueryResult() {
        loadJSONResourceIntoCollection("names_100.json")
        val query = QueryBuilder
            .select(
                SelectResult.property("name.first").`as`("firstname"),
                SelectResult.property("name.last").`as`("lastname"),
                SelectResult.property("gender"),
                SelectResult.property("contact.address.city")
            )
            .from(DataSource.collection(testCollection))

        verifyQuery(
            query,
            100
        ) { _, result ->
            assertEquals(4, result.count())
            assertEquals(result.getValue(0), result.getValue("firstname"))
            assertEquals(result.getValue(1), result.getValue("lastname"))
            assertEquals(result.getValue(2), result.getValue("gender"))
            assertEquals(result.getValue(3), result.getValue("city"))
        }
    }

    @Test
    fun testQueryProjectingKeys() {
        loadDocuments(100)

        val query = QueryBuilder
            .select(
                SelectResult.expression(Function.avg(Expression.property(TEST_DOC_SORT_KEY))),
                SelectResult.expression(Function.count(Expression.property(TEST_DOC_SORT_KEY))),
                SelectResult.expression(Function.min(Expression.property(TEST_DOC_SORT_KEY))).`as`("min"),
                SelectResult.expression(Function.max(Expression.property(TEST_DOC_SORT_KEY))),
                SelectResult.expression(Function.sum(Expression.property(TEST_DOC_SORT_KEY))).`as`("sum")
            )
            .from(DataSource.collection(testCollection))

        verifyQuery(
            query,
            1
        ) { _, result ->
            assertEquals(5, result.count())
            assertEquals(result.getValue(0), result.getValue("$1"))
            assertEquals(result.getValue(1), result.getValue("$2"))
            assertEquals(result.getValue(2), result.getValue("min"))
            assertEquals(result.getValue(3), result.getValue("$3"))
            assertEquals(result.getValue(4), result.getValue("sum"))
        }
    }

    @Test
    fun testQuantifiedOperators() {
        loadJSONResourceIntoCollection("names_100.json")

        val exprLikes = Expression.property("likes")
        val exprVarLike = ArrayExpression.variable("LIKE")

        // ANY:
        var query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))
            .where(ArrayExpression
                .any(exprVarLike)
                .`in`(exprLikes)
                .satisfies(exprVarLike.equalTo(Expression.string("climbing")))
            )

        val i = AtomicInt(0)
        val expected = arrayOf("doc-017", "doc-021", "doc-023", "doc-045", "doc-060")
        assertEquals(
            expected.size,
            verifyQueryWithEnumerator(
                query
            ) { _, result -> assertEquals(expected[i.fetchAndIncrement()], result.getString(0)) }
        )

        // EVERY:
        query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))
            .where(ArrayExpression
                .every(ArrayExpression.variable("LIKE"))
                .`in`(exprLikes)
                .satisfies(exprVarLike.equalTo(Expression.string("taxes")))
            )

        assertEquals(
            42,
            verifyQueryWithEnumerator(
                query
            ) { n, result -> if (n == 1) { assertEquals("doc-007", result.getString(0)) } }
        )

        // ANY AND EVERY:
        query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))
            .where(ArrayExpression
                .anyAndEvery(ArrayExpression.variable("LIKE"))
                .`in`(exprLikes)
                .satisfies(exprVarLike.equalTo(Expression.string("taxes")))
            )

        assertEquals(0, verifyQueryWithEnumerator(query) { _, _ -> })
    }

    @Test
    fun testAggregateFunctions() {
        loadDocuments(100)

        val query = QueryBuilder
            .select(
                SelectResult.expression(Function.avg(Expression.property(TEST_DOC_SORT_KEY))),
                SelectResult.expression(Function.count(Expression.property(TEST_DOC_SORT_KEY))),
                SelectResult.expression(Function.min(Expression.property(TEST_DOC_SORT_KEY))),
                SelectResult.expression(Function.max(Expression.property(TEST_DOC_SORT_KEY))),
                SelectResult.expression(Function.sum(Expression.property(TEST_DOC_SORT_KEY)))
            )
            .from(DataSource.collection(testCollection))

        verifyQuery(
            query,
            1
        ) { _, result ->
            when (val avg = result.getValue(0)) {
                // JVM is Float
                is Float -> assertEquals(50.5F, avg, 0.0F)
                // iOS is Double
                is Double -> assertEquals(50.5, avg, 0.0)
                null -> fail("avg is null")
                else -> fail("avg is $avg (${avg::class})")
            }
            assertEquals(100L, result.getValue(1) as Long)
            assertEquals(1L, result.getValue(2) as Long)
            assertEquals(100L, result.getValue(3) as Long)
            assertEquals(5050L, result.getValue(4) as Long)
        }
    }

    @Test
    fun testArrayFunctions() {
        val doc = MutableDocument()
        val array = MutableArray()
        array.addValue("650-123-0001")
        array.addValue("650-123-0002")
        doc.setValue("array", array)
        saveDocInCollection(doc)

        val exprArray = Expression.property("array")

        var query = QueryBuilder.select(SelectResult.expression(ArrayFunction.length(exprArray)))
            .from(DataSource.collection(testCollection))

        verifyQuery(query, 1) { _, result -> assertEquals(2, result.getInt(0)) }

        query = QueryBuilder
            .select(
                SelectResult.expression(ArrayFunction.contains(exprArray, Expression.string("650-123-0001"))),
                SelectResult.expression(ArrayFunction.contains(exprArray, Expression.string("650-123-0003")))
            )
            .from(DataSource.collection(testCollection))

        verifyQuery(
            query,
            1
        ) { _, result ->
            assertTrue(result.getBoolean(0))
            assertFalse(result.getBoolean(1))
        }
    }

//    @Test
//    fun testArrayFunctionsEmptyArgs() {
//        val exprArray = Expression.property("array")
//
//        assertFailsWith<IllegalArgumentException> {
//            ArrayFunction.contains(null, Expression.string("650-123-0001"))
//        }
//
//        assertFailsWith<IllegalArgumentException> { ArrayFunction.contains(exprArray, null) }
//
//        assertFailsWith<IllegalArgumentException> { ArrayFunction.length(null) }
//    }

    @Test
    fun testMathFunctions() {
        val key = "number"
        val num = 0.6
        val propNumber = Expression.property(key)

        val doc = MutableDocument()
        doc.setValue(key, num)
        saveDocInCollection(doc)

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
            verifyQuery(
                QueryBuilder.select(SelectResult.expression(f.expr)).from(DataSource.collection(testCollection)),
                1
            ) { _, result -> assertEquals(f.expected, result.getDouble(0), 1E-12, f.name) }
        }
    }

    @Test
    fun testStringFunctions() {
        val str = "  See you 18r  "
        val prop = Expression.property("greeting")

        val doc = MutableDocument()
        doc.setValue("greeting", str)
        saveDocInCollection(doc)

        var query = QueryBuilder
            .select(
                SelectResult.expression(Function.contains(prop, Expression.string("8"))),
                SelectResult.expression(Function.contains(prop, Expression.string("9")))
            )
            .from(DataSource.collection(testCollection))

        verifyQuery(
            query,
            1
        ) { _, result ->
            assertTrue(result.getBoolean(0))
            assertFalse(result.getBoolean(1))
        }

        // Length
        query = QueryBuilder.select(SelectResult.expression(Function.length(prop)))
            .from(DataSource.collection(testCollection))

        verifyQuery(query, 1) { _, result -> assertEquals(str.length, result.getInt(0)) }

        // Lower, Ltrim, Rtrim, Trim, Upper:
        query = QueryBuilder
            .select(
                SelectResult.expression(Function.lower(prop)),
                SelectResult.expression(Function.ltrim(prop)),
                SelectResult.expression(Function.rtrim(prop)),
                SelectResult.expression(Function.trim(prop)),
                SelectResult.expression(Function.upper(prop))
            )
            .from(DataSource.collection(testCollection))

        verifyQuery(
            query,
            1
        ) { _, result ->
            assertEquals(str.lowercase(), result.getString(0))
            assertEquals(str.replace("^\\s+".toRegex(), ""), result.getString(1))
            assertEquals(str.replace("\\s+$".toRegex(), ""), result.getString(2))
            assertEquals(str.trim { it <= ' ' }, result.getString(3))
            assertEquals(str.uppercase(), result.getString(4))
        }
    }

    @Test
    fun testSelectAll() {
        loadDocuments(100)

        val collectionName = testCollection.name

        // SELECT *
        verifyQuery(
            QueryBuilder.select(SelectResult.all()).from(DataSource.collection(testCollection)),
            100
        ) { n, result ->
            assertEquals(1, result.count())
            val a1 = result.getDictionary(0)!!
            val a2 = result.getDictionary(collectionName)!!
            assertEquals(n, a1.getInt(TEST_DOC_SORT_KEY))
            assertEquals(100 - n, a1.getInt(TEST_DOC_REV_SORT_KEY))
            assertEquals(n, a2.getInt(TEST_DOC_SORT_KEY))
            assertEquals(100 - n, a2.getInt(TEST_DOC_REV_SORT_KEY))
        }

        // SELECT *, number1
        var query = QueryBuilder.select(SelectResult.all(), SelectResult.property(TEST_DOC_SORT_KEY))
            .from(DataSource.collection(testCollection))

        verifyQuery(
            query,
            100
        ) { n, result ->
            assertEquals(2, result.count())
            val a1 = result.getDictionary(0)!!
            val a2 = result.getDictionary(collectionName)!!
            assertEquals(n, a1.getInt(TEST_DOC_SORT_KEY))
            assertEquals(100 - n, a1.getInt(TEST_DOC_REV_SORT_KEY))
            assertEquals(n, a2.getInt(TEST_DOC_SORT_KEY))
            assertEquals(100 - n, a2.getInt(TEST_DOC_REV_SORT_KEY))
            assertEquals(n, result.getInt(1))
            assertEquals(n, result.getInt(TEST_DOC_SORT_KEY))
        }

        // SELECT testdb.*
        query = QueryBuilder.select(SelectResult.all().from(collectionName))
            .from(DataSource.collection(testCollection).`as`(collectionName))

        verifyQuery(
            query,
            100
        ) { n, result ->
            assertEquals(1, result.count())
            val a1 = result.getDictionary(0)!!
            val a2 = result.getDictionary(collectionName)!!
            assertEquals(n, a1.getInt(TEST_DOC_SORT_KEY))
            assertEquals(100 - n, a1.getInt(TEST_DOC_REV_SORT_KEY))
            assertEquals(n, a2.getInt(TEST_DOC_SORT_KEY))
            assertEquals(100 - n, a2.getInt(TEST_DOC_REV_SORT_KEY))
        }

        // SELECT testdb.*, testdb.number1
        query = QueryBuilder
            .select(
                SelectResult.all().from(collectionName),
                SelectResult.expression(Expression.property(TEST_DOC_SORT_KEY).from(collectionName))
            )
            .from(DataSource.collection(testCollection).`as`(collectionName))

        verifyQuery(
            query,
            100
        ) { n, result ->
            assertEquals(2, result.count())
            val a1 = result.getDictionary(0)!!
            val a2 = result.getDictionary(collectionName)!!
            assertEquals(n, a1.getInt(TEST_DOC_SORT_KEY))
            assertEquals(100 - n, a1.getInt(TEST_DOC_REV_SORT_KEY))
            assertEquals(n, a2.getInt(TEST_DOC_SORT_KEY))
            assertEquals(100 - n, a2.getInt(TEST_DOC_REV_SORT_KEY))
            assertEquals(n, result.getInt(1))
            assertEquals(n, result.getInt(TEST_DOC_SORT_KEY))
        }
    }

    // With no locale, characters with diacritics should be
    // treated as the original letters A, E, I, O, U,
    @Test
    fun testUnicodeCollationWithLocaleNone() {
        createAlphaDocs()

        val noLocale = Collation.unicode()
            .setLocale(null)
            .setIgnoreCase(false)
            .setIgnoreAccents(false)

        val query = QueryBuilder.select(SelectResult.property("string"))
            .from(DataSource.collection(testCollection))
            .orderBy(Ordering.expression(Expression.property("string").collate(noLocale)))

        val expected = arrayOf("A", "Å", "B", "Z")
        verifyQuery(query, expected.size) { n, result -> assertEquals(expected[n - 1], result.getString(0)) }
    }

    // In the Spanish alphabet, the six characters with diacritics Á, É, Í, Ó, Ú, Ü
    // are treated as the original letters A, E, I, O, U,
    @Test
    fun testUnicodeCollationWithLocaleSpanish() {
        createAlphaDocs()

        val localeEspanol: Collation = Collation.unicode()
            .setLocale("es")
            .setIgnoreCase(false)
            .setIgnoreAccents(false)

        val query = QueryBuilder.select(SelectResult.property("string"))
            .from(DataSource.collection(testCollection))
            .orderBy(Ordering.expression(Expression.property("string").collate(localeEspanol)))

        val expected = arrayOf("A", "Å", "B", "Z")
        verifyQuery(query, expected.size) { n, result -> assertEquals(expected[n - 1], result.getString(0)) }
    }

    // In the Swedish alphabet, there are three extra vowels
    // placed at its end (..., X, Y, Z, Å, Ä, Ö),
    // Early versions of Android do not support the Swedish Locale
    @Test
    fun testUnicodeCollationWithLocaleSwedish() {
        createAlphaDocs()

        val query = QueryBuilder.select(SelectResult.property("string"))
            .from(DataSource.collection(testCollection))
            .orderBy(Ordering.expression(Expression.property("string")
                .collate(Collation.unicode()
                    .setLocale("sv")
                    .setIgnoreCase(false)
                    .setIgnoreAccents(false)
                )
            ))

        val expected = arrayOf("A", "B", "Z", "Å")
        verifyQuery(query, expected.size) { n, result -> assertEquals(expected[n - 1], result.getString(0)) }
    }

    @Test
    fun testCompareWithUnicodeCollation() {
        class CollationTest(
            val value: String,
            val test: String,
            val mode: Boolean,
            val collation: Collation
        ) {
            override fun toString(): String {
                return "test '" + value + "' " + (if ((mode)) "=" else "<") + " '" + test + "'"
            }
        }

        val bothSensitive = Collation.unicode().setLocale(null).setIgnoreCase(false).setIgnoreAccents(false)
        val accentSensitive = Collation.unicode().setLocale(null).setIgnoreCase(true).setIgnoreAccents(false)
        val caseSensitive = Collation.unicode().setLocale(null).setIgnoreCase(false).setIgnoreAccents(true)
        val noSensitive = Collation.unicode().setLocale(null).setIgnoreCase(true).setIgnoreAccents(true)

        val testData = listOf(
            // Edge cases: empty and 1-char strings:
            CollationTest("", "", true, bothSensitive),
            CollationTest("", "a", false, bothSensitive),
            CollationTest("a", "a", true, bothSensitive),

            // Case-sensitive: lowercase come first by unicode rules:
            CollationTest("a", "A", false, bothSensitive),
            CollationTest("abc", "abc", true, bothSensitive),
            CollationTest("Aaa", "abc", false, bothSensitive),
            CollationTest("abc", "abC", false, bothSensitive),
            CollationTest("AB", "abc", false, bothSensitive),

            // Case-insensitive:
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
            CollationTest("Ähnlichkeit", "apple", false, bothSensitive), // Because 'h'-vs-'p' beats 'Ä'-vs-'a'
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
            val doc = saveDocInCollection(mDoc)

            val test = Expression.value(data.test)
            var comparison = Expression.property("value").collate(data.collation)
            comparison = if (data.mode) comparison.equalTo(test) else comparison.lessThan(test)

            verifyQuery(
                QueryBuilder.select().from(DataSource.collection(testCollection)).where(comparison),
                1
            ) { n, result ->
                assertEquals(1, n)
                assertNotNull(result)
            }

            testCollection.delete(doc)
        }
    }

    // This is a pretty finicky test: the numbers are important.
    // It will fail by timing out; you'll have to figure out why.
    @Test
    fun testLiveQuery() = runBlocking {
        val firstLoad = loadDocuments(100, 20)

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))
            .where(Expression.property(TEST_DOC_SORT_KEY).lessThan(Expression.intValue(110)))
            .orderBy(Ordering.property(TEST_DOC_SORT_KEY).ascending())

        val latch1 = CountDownLatch(1)
        val latch2 = CountDownLatch(1)
        val secondBatch: MutableList<String> = ConcurrentMutableList()
        val listener: QueryChangeListener = { change ->
            val rs = change.results
            var count = 0
            while (true) {
                val r = rs?.next() ?: break
                count++
                // The first run of this query should see a result set with 10 results:
                // There are 20 docs in the db, with sort keys 100 .. 119. Only 10
                // meet the where criteria < 110: (100 .. 109)
                if (latch1.getCount() > 0) {
                    if (count >= 10) { latch1.countDown() }
                    continue
                }

                // When we add 10 more documents, sort keys 1 .. 10, the live
                // query should report 20 docs matching the query
                secondBatch.add(r.getString("id")!!)
                if (count >= 20) { latch2.countDown() }
            }
        }

        try {
            query.addChangeListener(testSerialCoroutineContext, listener).use {
                assertTrue(latch1.await(STD_TIMEOUT_SEC.seconds))
                // create some more docs
                val secondLoad = loadDocuments(10)

                // wait till listener sees them all
                assertTrue(latch2.await(LONG_TIMEOUT_SEC.seconds))

                // verify that the listener saw, in the second batch
                // the first 10 of the first load of documents
                val expected = firstLoad.subList(0, 10).map(Document::id).toMutableList()
                // and all of the second load.
                expected.addAll(secondLoad.map(Document::id))
                expected.sort()
                secondBatch.sort()
                assertEquals(expected, secondBatch.toList())
            }
        } // Catch clause prevents Windows compiler error
        catch (e: Exception) {
            // Cause isn't logged on native platforms...
            // https://youtrack.jetbrains.com/issue/KT-62794
            println("Cause:")
            println(e.message)
            println(e.stackTraceToString())
            throw AssertionError("Unexpected exception", e)
        }
    }

    @Test
    fun testLiveQueryNoUpdate1() = runBlocking { liveQueryNoUpdate { } }

    @Test
    fun testLiveQueryNoUpdate2() = runBlocking {
        liveQueryNoUpdate { change ->
            val rs = change.results
            @Suppress("ControlFlowWithEmptyBody")
            while (rs?.next() != null) { }
        }
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1356
    @Test
    fun testCountFunctions() {
        loadDocuments(100)

        val query = QueryBuilder
            .select(SelectResult.expression(Function.count(Expression.property(TEST_DOC_SORT_KEY))))
            .from(DataSource.collection(testCollection))

        verifyQuery(query, 1) { _, result -> assertEquals(100L, result.getValue(0) as Long) }
    }

    @Test
    fun testJoinWithArrayContains() {
        // Data preparation
        // Hotels
        val hotel1 = MutableDocument()
        hotel1.setString("type", "hotel")
        hotel1.setString("name", "Hilton")
        saveDocInCollection(hotel1)

        val hotel2 = MutableDocument()
        hotel2.setString("type", "hotel")
        hotel2.setString("name", "Sheraton")
        saveDocInCollection(hotel2)

        val hotel3 = MutableDocument()
        hotel3.setString("type", "hotel")
        hotel3.setString("name", "Marriott")
        saveDocInCollection(hotel3)

        // Bookmark
        val bookmark1 = MutableDocument()
        bookmark1.setString("type", "bookmark")
        bookmark1.setString("title", "Bookmark For Hawaii")
        val hotels1 = MutableArray()
        hotels1.addString("hotel1")
        hotels1.addString("hotel2")
        bookmark1.setArray("hotels", hotels1)
        saveDocInCollection(bookmark1)

        val bookmark2 = MutableDocument()
        bookmark2.setString("type", "bookmark")
        bookmark2.setString("title", "Bookmark for New York")
        val hotels2 = MutableArray()
        hotels2.addString("hotel3")
        bookmark2.setArray("hotels", hotels2)
        saveDocInCollection(bookmark2)

        QueryBuilder
            .select(SelectResult.all().from("main"), SelectResult.all().from("secondary"))
            .from(DataSource.collection(testCollection).`as`("main"))
            .join(Join.join(DataSource.collection(testCollection).`as`("secondary"))
                .on(ArrayFunction.contains(Expression.property("hotels").from("main"), Meta.id.from("secondary")))
            )
            .where(Expression.property("type").from("main").equalTo(Expression.string("bookmark")))
    }

//    @Test
//    fun testJoinWithEmptyArgs1() {
//        assertFailsWith<IllegalArgumentException> {
//            QueryBuilder.select(SelectResult.all())
//                .from(DataSource.collection(testCollection).`as`("main"))
//                .join(null)
//        }
//    }
//
//    @Test
//    fun testJoinWithEmptyArgs2() {
//        assertFailsWith<IllegalArgumentException> {
//            QueryBuilder.select(SelectResult.all())
//                .from(DataSource.collection(testCollection).`as`("main"))
//                .where(null)
//        }
//    }
//
//    @Test
//    fun testJoinWithEmptyArgs3() {
//        assertFailsWith<IllegalArgumentException> {
//            QueryBuilder.select(SelectResult.all())
//                .from(DataSource.collection(testCollection).`as`("main"))
//                .groupBy(null)
//        }
//    }
//
//    @Test
//    fun testJoinWithEmptyArgs4() {
//        assertFailsWith<IllegalArgumentException> {
//            QueryBuilder.select(SelectResult.all())
//                .from(DataSource.collection(testCollection).`as`("main"))
//                .orderBy(null)
//        }
//    }
//
//    @Test
//    fun testJoinWithEmptyArgs5() {
//        assertFailsWith<IllegalArgumentException> {
//            QueryBuilder.select(SelectResult.all())
//                .from(DataSource.collection(testCollection).`as`("main"))
//                .limit(null)
//        }
//    }
//
//    @Test
//    fun testJoinWithEmptyArgs6() {
//        assertFailsWith<IllegalArgumentException> {
//            QueryBuilder.select(SelectResult.all())
//                .from(DataSource.collection(testCollection).`as`("main"))
//                .limit(null, null)
//        }
//    }

    //https://github.com/couchbase/couchbase-lite-android/issues/1785
    @Test
    fun testResultToMapWithBoolean() {
        val exam1 = MutableDocument()
        exam1.setString("exam type", "final")
        exam1.setString("question", "There are 45 states in the US.")
        exam1.setBoolean("answer", false)
        saveDocInCollection(exam1)

        val exam2 = MutableDocument()
        exam2.setString("exam type", "final")
        exam2.setString("question", "There are 100 senators in the US.")
        exam2.setBoolean("answer", true)
        saveDocInCollection(exam2)

        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(testCollection))
            .where(Expression.property("exam type").equalTo(Expression.string("final"))
                .and(Expression.property("answer").equalTo(Expression.booleanValue(true)))
            )

        val collectionName: String = testCollection.name
        verifyQuery(
            query,
            1
        ) { _, result ->
            val maps = result.toMap()
            assertNotNull(maps)
            val map = maps[collectionName] as Map<*, *>?
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
    fun testResultToMapWithBoolean2() {
        val mDoc = MutableDocument()
        mDoc.setString("exam type", "final")
        mDoc.setString("question", "There are 45 states in the US.")
        mDoc.setBoolean("answer", true)

        saveDocInCollection(mDoc)

        val query = QueryBuilder
            .select(
                SelectResult.property("exam type"),
                SelectResult.property("question"),
                SelectResult.property("answer")
            )
            .from(DataSource.collection(testCollection))
            .where(Meta.id.equalTo(Expression.string(mDoc.id)))

        verifyQuery(query, 1) { _, result -> assertTrue(result.toMap()["answer"] as Boolean) }
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1385
    @Test
    fun testQueryDeletedDocument() {
        // Insert two documents
        val task1 = createTaskDocument("Task 1", false)
        val task2 = createTaskDocument("Task 2", false)
        assertEquals(2, testCollection.count)

        // query documents before deletion
        val query = QueryBuilder.select(SelectResult.expression(Meta.id), SelectResult.all())
            .from(DataSource.collection(testCollection))
            .where(Expression.property("type").equalTo(Expression.string("task")))

        verifyQuery(query, 2) { _, _ -> }

        // delete artifacts from task 1
        testCollection.delete(task1)
        assertEquals(1, testCollection.count)
        assertNull(testCollection.getDocument(task1.id))

        // query documents again after deletion
        verifyQuery(query, 1) { _, result -> assertEquals(task2.id, result.getString(0)) }
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1389
    @Test
    fun testQueryWhereBooleanExpression() {
        // STEP 1: Insert three documents
        createTaskDocument("Task 1", false)
        createTaskDocument("Task 2", true)
        createTaskDocument("Task 3", true)
        assertEquals(3, testCollection.count)

        val exprType = Expression.property("type")
        val exprComplete = Expression.property("complete")
        val srCount = SelectResult.expression(Function.count(Expression.intValue(1)))

        // regular query - true
        var query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(testCollection))
            .where(exprType.equalTo(Expression.string("task"))
                .and(exprComplete.equalTo(Expression.booleanValue(true)))
            )

        var numRows = verifyQueryWithEnumerator(
            query
        ) { _, result ->
            val dict = result.getDictionary(testCollection.name)!!
            assertTrue(dict.getBoolean("complete"))
            assertEquals("task", dict.getString("type"))
            assertTrue(dict.getString("title")!!.startsWith("Task "))
        }
        assertEquals(2, numRows)

        // regular query - false
        query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(testCollection))
            .where(exprType.equalTo(Expression.string("task"))
                .and(exprComplete.equalTo(Expression.booleanValue(false)))
            )

        numRows = verifyQueryWithEnumerator(
            query
        ) { _, result ->
            val dict = result.getDictionary(testCollection.name)!!
            assertFalse(dict.getBoolean("complete"))
            assertEquals("task", dict.getString("type"))
            assertTrue(dict.getString("title")!!.startsWith("Task "))
        }
        assertEquals(1, numRows)

        // aggregation query - true
        query = QueryBuilder.select(srCount)
            .from(DataSource.collection(testCollection))
            .where(exprType.equalTo(Expression.string("task"))
                .and(exprComplete.equalTo(Expression.booleanValue(true)))
            )

        numRows = verifyQueryWithEnumerator(query) { _, result -> assertEquals(2, result.getInt(0)) }
        assertEquals(1, numRows)

        // aggregation query - false
        query = QueryBuilder.select(srCount)
            .from(DataSource.collection(testCollection))
            .where(exprType.equalTo(Expression.string("task"))
                .and(exprComplete.equalTo(Expression.booleanValue(false)))
            )

        numRows = verifyQueryWithEnumerator(query) { _, result -> assertEquals(1, result.getInt(0)) }
        assertEquals(1, numRows)
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1413
    @Test
    fun testJoinAll() {
        loadDocuments(100)

        val doc1 = MutableDocument()
        doc1.setValue("theone", 42)
        saveDocInCollection(doc1)

        val query = QueryBuilder.select(SelectResult.all().from("main"), SelectResult.all().from("secondary"))
            .from(DataSource.collection(testCollection).`as`("main"))
            .join(Join.join(DataSource.collection(testCollection).`as`("secondary"))
                .on(Expression.property(TEST_DOC_SORT_KEY).from("main")
                    .equalTo(Expression.property("theone").from("secondary"))
                )
            )

        verifyQuery(
            query,
            1
        ) { _, result ->
            val mainAll1 = result.getDictionary(0)!!
            val mainAll2 = result.getDictionary("main")!!
            val secondAll1 = result.getDictionary(1)!!
            val secondAll2 = result.getDictionary("secondary")!!
            assertEquals(42, mainAll1.getInt(TEST_DOC_SORT_KEY))
            assertEquals(42, mainAll2.getInt(TEST_DOC_SORT_KEY))
            assertEquals(58, mainAll1.getInt(TEST_DOC_REV_SORT_KEY))
            assertEquals(58, mainAll2.getInt(TEST_DOC_REV_SORT_KEY))
            assertEquals(42, secondAll1.getInt("theone"))
            assertEquals(42, secondAll2.getInt("theone"))
        }
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1413
    @Test
    fun testJoinByDocID() {
        // Load a bunch of documents and pick one randomly
        val doc1: Document = loadDocuments(100)[Random.nextInt(100)]

        val mDoc = MutableDocument()
        mDoc.setValue("theone", 42)
        mDoc.setString("numberID", doc1.id) // document ID of number documents.
        saveDocInCollection(mDoc)

        val query = QueryBuilder
            .select(
                SelectResult.expression(Meta.id.from("main")).`as`("mainDocID"),
                SelectResult.expression(Meta.id.from("secondary")).`as`("secondaryDocID"),
                SelectResult.expression(Expression.property("theone").from("secondary"))
            )
            .from(DataSource.collection(testCollection).`as`("main"))
            .join(Join.join(DataSource.collection(testCollection).`as`("secondary"))
                .on(Meta.id.from("main").equalTo(Expression.property("numberID").from("secondary")))
            )

        verifyQuery(
            query,
            1
        ) { n, result ->
            assertEquals(1, n)

            val doc3 = testCollection.getDocument(result.getString("mainDocID")!!)!!
            assertEquals(doc1.getInt(TEST_DOC_SORT_KEY), doc3.getInt(TEST_DOC_SORT_KEY))
            assertEquals(doc1.getInt(TEST_DOC_REV_SORT_KEY), doc3.getInt(TEST_DOC_REV_SORT_KEY))

            // data from secondary
            assertEquals(mDoc.id, result.getString("secondaryDocID"))
            assertEquals(42, result.getInt("theone"))
        }
    }

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

        val expected = listOf(
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

        // replace system default locale value with expected null
        // and number values as booleans
        fun Any.massageJson(expectedCollation: Map<String, Any?>): Map<String, Any?> {
            @Suppress("UNCHECKED_CAST")
            this as Map<String, Any?>
            return if (expectedCollation["LOCALE"] == null) {
                mapValues { (key, value) ->
                    if (key == "LOCALE" && value != null) {
                        println("Setting $key $value to null")
                        null
                    } else {
                        value
                    }
                }
            } else {
                this
            }.mapValues { (key, value) ->
                if (value is Number) {
                    val boolean = value != 0
                    println("Setting $key $value to $boolean")
                    boolean
                } else {
                    value
                }
            }
        }

        for (i in collations.indices) {
            // TODO: null locale uses system default on iOS, also some numbers for booleans
            //  https://forums.couchbase.com/t/unicode-collation-locale-null-or-device-locale/34103
            //assertEquals(expected[i], collations[i].asJSON())
            val expectedCollation = expected[i]
            @Suppress("UNNECESSARY_SAFE_CALL", "KotlinRedundantDiagnosticSuppress")
            val collation = collations[i].asJSON()?.massageJson(expectedCollation)
            assertEquals(expectedCollation, collation)
        }
    }

    @Test
    fun testAllComparison() {
        val values = arrayOf("Apple", "Aardvark", "Ångström", "Zebra", "äpple")
        for (value in values) {
            val doc = MutableDocument()
            doc.setString("hey", value)
            saveDocInCollection(doc)
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
                .from(DataSource.collection(testCollection))
                .orderBy(Ordering.expression(property.collate(data[1] as Collation)))

            val list = mutableListOf<String?>()
            verifyQueryWithEnumerator(query) { _, result -> list.add(result.getString(0)) }
            assertEquals(data[2], list)
        }
    }

    @Test
    fun testDeleteDatabaseWithActiveLiveQuery() = runBlocking {
        val latch1 = CountDownLatch(1)
        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))

        query.addChangeListener(testSerialCoroutineContext) { latch1.countDown() }.use {
            assertTrue(latch1.await(STD_TIMEOUT_SEC.seconds))
            deleteDb(testDatabase)
        }
    }

    @Test
    fun testCloseDatabaseWithActiveLiveQuery() = runBlocking {
        val latch = CountDownLatch(1)
        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))

        val token = query.addChangeListener(testSerialCoroutineContext) { latch.countDown() }
        try {
            assertTrue(latch.await(STD_TIMEOUT_SEC.seconds))
            closeDb(testDatabase)
        } finally { token.remove() }
    }

    @Test
    fun testFunctionCount() {
        loadDocuments(100)

        val doc = MutableDocument()
        doc.setValue("string", "STRING")
        doc.setValue("date", null)
        saveDocInCollection(doc)

        val query = QueryBuilder
            .select(
                SelectResult.expression(Function.count(Expression.property(TEST_DOC_SORT_KEY))),
                SelectResult.expression(Function.count(Expression.intValue(1))),
                SelectResult.expression(Function.count(Expression.string("*"))),
                SelectResult.expression(Function.count(Expression.all())),
                SelectResult.expression(Function.count(Expression.property("string"))),
                SelectResult.expression(Function.count(Expression.property("date"))),
                SelectResult.expression(Function.count(Expression.property("notExist")))
            )
            .from(DataSource.collection(testCollection))

        verifyQuery(
            query,
            1
        ) { _, result ->
            assertEquals(100L, result.getValue(0) as Long)
            assertEquals(101L, result.getValue(1) as Long)
            assertEquals(101L, result.getValue(2) as Long)
            assertEquals(101L, result.getValue(3) as Long)
            assertEquals(1L, result.getValue(4) as Long)
            assertEquals(1L, result.getValue(5) as Long)
            assertEquals(0L, result.getValue(6) as Long)
        }
    }

    @Test
    fun testFunctionCountAll() {
        loadDocuments(100)

        // SELECT count(*)
        var query = QueryBuilder.select(SelectResult.expression(Function.count(Expression.all())))
            .from(DataSource.collection(testCollection))

        verifyQuery(
            query,
            1
        ) { _, result ->
            assertEquals(1, result.count())
            assertEquals(100L, result.getValue(0) as Long)
        }

        // SELECT count(testdb.*)
        query = QueryBuilder.select(SelectResult.expression(Function.count(Expression.all()
                .from(testCollection.name)
            )))
            .from(DataSource.collection(testCollection).`as`(testCollection.name))

        verifyQuery(
            query,
            1
        ) { _, result ->
            assertEquals(1, result.count())
            assertEquals(100L, result.getValue(0) as Long)
        }
    }

    @Test
    fun testResultSetEnumeration() {
        val docIds = loadDocuments(5).map(Document::id)

        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))
            .orderBy(Ordering.property(TEST_DOC_SORT_KEY))

        // Type 1: Enumeration by ResultSet.next()
        var i = 0
        query.execute().use { rs ->
            while (true) {
                val result = rs.next() ?: break
                assertTrue(docIds.contains(result.getString(0)))
                i++
            }
            assertEquals(docIds.size, i)
            assertNull(rs.next())
            assertEquals(0, rs.allResults().size)
        }

        // Type 2: Enumeration by ResultSet.iterator()
        i = 0
        query.execute().use { rs ->
            for (r in rs) {
                assertTrue(docIds.contains(r.getString(0)))
                i++
            }
            assertEquals(docIds.size, i)
            assertNull(rs.next())
            assertEquals(0, rs.allResults().size)
        }

        // Type 3: Enumeration by ResultSet.allResults().get(int index)
        i = 0
        query.execute().use { rs ->
            val list = rs.allResults()
            for (r in list) {
                assertTrue(docIds.contains(r.getString(0)))
                i++
            }
            assertEquals(docIds.size, i)
            assertNull(rs.next())
            assertEquals(0, rs.allResults().size)
        }

        // Type 4: Enumeration by ResultSet.allResults().iterator()
        i = 0
        query.execute().use { rs ->
            for (r in rs.allResults()) {
                assertTrue(docIds.contains(r.getString(0)))
                i++
            }
            assertEquals(docIds.size, i)
            assertNull(rs.next())
            assertEquals(0, rs.allResults().size)
        }
    }

    @Test
    fun testGetAllResults() {
        val docIds = loadDocuments(5).map(Document::id)

        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))
            .orderBy(Ordering.property(TEST_DOC_SORT_KEY))

        // Get all results by get(int)
        var i = 0
        query.execute().use { rs ->
            val results = rs.allResults()
            for (j in results.indices) {
                assertTrue(docIds.contains(results[j].getString(0)))
                i++
            }
            assertEquals(docIds.size, results.size)
            assertEquals(docIds.size, i)
            assertNull(rs.next())
            assertEquals(0, rs.allResults().size)
        }

        // Get all results by iterator
        i = 0
        query.execute().use { rs ->
            val results = rs.allResults()
            for (r in results) {
                assertTrue(docIds.contains(r.getString(0)))
                i++
            }
            assertEquals(docIds.size, results.size)
            assertEquals(docIds.size, i)
            assertNull(rs.next())
            assertEquals(0, rs.allResults().size)
        }

        // Partial enumerating then get all results:
        i = 0
        query.execute().use { rs ->
            assertNotNull(rs.next())
            assertNotNull(rs.next())
            val results = rs.allResults()
            for (r in results) {
                assertTrue(docIds.contains(r.getString(0)))
                i++
            }
            assertEquals(docIds.size - 2, results.size)
            assertEquals(docIds.size - 2, i)
            assertNull(rs.next())
            assertEquals(0, rs.allResults().size)
        }
    }

    @Test
    fun testResultSetEnumerationZeroResults() {
        loadDocuments(5)

        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))
            .where(Expression.property(TEST_DOC_SORT_KEY).`is`(Expression.intValue(100)))
            .orderBy(Ordering.property(TEST_DOC_SORT_KEY))

        // Type 1: Enumeration by ResultSet.next()
        var i = 0
        query.execute().use { rs ->
            while (rs.next() != null) { i++ }
            assertEquals(0, i)
            assertNull(rs.next())
            assertEquals(0, rs.allResults().size)
        }

        // Type 2: Enumeration by ResultSet.iterator()
        i = 0
        query.execute().use { rs ->
            for (ignored in rs) { i++ }
            assertEquals(0, i)
            assertNull(rs.next())
            assertEquals(0, rs.allResults().size)
        }

        // Type 3: Enumeration by ResultSet.allResults().get(int index)
        i = 0
        query.execute().use { rs ->
            val list = rs.allResults()
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
            for (ignored in rs.allResults()) { i++ }
            assertEquals(0, i)
            assertNull(rs.next())
            assertEquals(0, rs.allResults().size)
        }
    }

    @Test
    fun testMissingValue() {
        val doc1 = MutableDocument()
        doc1.setValue("name", "Scott")
        doc1.setValue("address", null)
        saveDocInCollection(doc1)

        val query = QueryBuilder.select(
                SelectResult.property("name"),
                SelectResult.property("address"),
                SelectResult.property("age")
            )
            .from(DataSource.collection(testCollection))

        // Array:
        verifyQuery(
            query,
            1
        ) { _, result ->
            assertEquals(3, result.count())
            assertEquals("Scott", result.getString(0))
            assertNull(result.getValue(1))
            assertNull(result.getValue(2))
            assertEquals(listOf("Scott", null, null), result.toList())
        }

        // Dictionary:
        verifyQuery(
            query,
            1
        ) { _, result ->
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
    fun testExpressionNot() {
        loadDocuments(10)

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.property(TEST_DOC_SORT_KEY))
            .from(DataSource.collection(testCollection))
            .where(Expression.not(Expression.property(TEST_DOC_SORT_KEY)
                .between(Expression.intValue(3), Expression.intValue(5))
            ))
            .orderBy(Ordering.expression(Expression.property(TEST_DOC_SORT_KEY)).ascending())

        verifyQuery(
            query,
            7
        ) { n, result ->
            if (n < 3) { assertEquals(n, result.getInt(TEST_DOC_SORT_KEY)) }
            else { assertEquals(n + 3, result.getInt(TEST_DOC_SORT_KEY)) }
        }
    }

    @Test
    fun testLimitValueIsLargerThanResult() {
        val docIds = loadDocuments(4)

        val query = QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.collection(testCollection))
            .limit(Expression.intValue(10))

        verifyQuery(query, docIds.size) { _, _ -> }
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1614
    @Test
    fun testFTSStemming() {
        val mDoc0 = MutableDocument()
        mDoc0.setString("content", "hello")
        mDoc0.setInt(TEST_DOC_SORT_KEY, 0)
        saveDocInCollection(mDoc0)

        val mDoc1 = MutableDocument()
        mDoc1.setString("content", "beauty")
        mDoc1.setInt(TEST_DOC_SORT_KEY, 10)
        saveDocInCollection(mDoc1)

        val mDoc2 = MutableDocument()
        mDoc2.setString("content", "beautifully")
        mDoc2.setInt(TEST_DOC_SORT_KEY, 20)
        saveDocInCollection(mDoc2)

        val mDoc3 = MutableDocument()
        mDoc3.setString("content", "beautiful")
        mDoc3.setInt(TEST_DOC_SORT_KEY, 30)
        saveDocInCollection(mDoc3)

        val mDoc4 = MutableDocument()
        mDoc4.setString("content", "pretty")
        mDoc4.setInt(TEST_DOC_SORT_KEY, 40)
        saveDocInCollection(mDoc4)

        val ftsIndex = IndexBuilder.fullTextIndex(FullTextIndexItem.property("content"))
        ftsIndex.setLanguage("en")
        testCollection.createIndex("ftsIndex", ftsIndex)
        val idx = Expression.fullTextIndex("ftsIndex")

        val expectedIDs = arrayOf(mDoc1.id, mDoc2.id, mDoc3.id)
        val expectedContents = arrayOf("beauty", "beautifully", "beautiful")

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.property("content"))
            .from(DataSource.collection(testCollection))
            .where(FullTextFunction.match(idx, "beautiful"))
            .orderBy(Ordering.property(TEST_DOC_SORT_KEY).ascending())

        verifyQuery(
            query,
            3
        ) { n, result ->
            assertEquals(expectedIDs[n - 1], result.getString("id"))
            assertEquals(expectedContents[n - 1], result.getString("content"))
        }
    }

    // https://github.com/couchbase/couchbase-lite-net/blob/master/src/Couchbase.Lite.Tests.Shared/QueryTest.cs#L1721
    @Test
    fun testFTSStemming2() {
        testCollection.createIndex(
            "passageIndex",
            IndexBuilder.fullTextIndex(FullTextIndexItem.property("passage")).setLanguage("en")
        )
        val idx = Expression.fullTextIndex("passageIndex")

        testCollection.createIndex(
            "passageIndexStemless",
            IndexBuilder.fullTextIndex(FullTextIndexItem.property("passage")).setLanguage(null)
        )
        val stemlessIdx = Expression.fullTextIndex("passageIndexStemless")

        val mDoc1 = MutableDocument()
        mDoc1.setString("passage", "The boy said to the child, 'Mommy, I want a cat.'")
        saveDocInCollection(mDoc1)

        val mDoc2 = MutableDocument()
        mDoc2.setString("passage", "The mother replied 'No, you already have too many cats.'")
        saveDocInCollection(mDoc2)

        var query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))
            .where(FullTextFunction.match(idx, "cat"))

        val expected = arrayOf(mDoc1.id, mDoc2.id)

        verifyQuery(query, 2) { n, result -> assertEquals(expected[n - 1], result.getString(0)) }

        query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))
            .where(FullTextFunction.match(stemlessIdx, "cat"))

        verifyQuery(query, 1) { n, result -> assertEquals(expected[n - 1], result.getString(0)) }
    }

    // 3.1. Set Operations Using The Enhanced Query Syntax
    // https://www.sqlite.org/fts3.html#_set_operations_using_the_enhanced_query_syntax
    // https://github.com/couchbase/couchbase-lite-android/issues/1620
    @Test
    fun testFTSSetOperations() {
        val mDoc1 = MutableDocument()
        mDoc1.setString("content", "a database is a software system")
        mDoc1.setInt(TEST_DOC_SORT_KEY, 100)
        saveDocInCollection(mDoc1)

        val mDoc2 = MutableDocument()
        mDoc2.setString("content", "sqlite is a software system")
        mDoc2.setInt(TEST_DOC_SORT_KEY, 200)
        saveDocInCollection(mDoc2)

        val mDoc3 = MutableDocument()
        mDoc3.setString("content", "sqlite is a database")
        mDoc3.setInt(TEST_DOC_SORT_KEY, 300)
        saveDocInCollection(mDoc3)

        val ftsIndex = IndexBuilder.fullTextIndex(FullTextIndexItem.property("content"))
        testCollection.createIndex("ftsIndex", ftsIndex)
        val idx = Expression.fullTextIndex("ftsIndex")

        // The enhanced query syntax
        // https://www.sqlite.org/fts3.html#_set_operations_using_the_enhanced_query_syntax

        // AND binary set operator
        var query = QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.property("content"))
            .from(DataSource.collection(testCollection))
            .where(FullTextFunction.match(idx, "sqlite AND database"))
            .orderBy(Ordering.property(TEST_DOC_SORT_KEY).ascending())
        verifyQuery(query, 1) { _, result -> assertEquals(mDoc3.id, result.getString("id")) }

        // implicit AND operator
        query = QueryBuilder.select(SelectResult.expression(Meta.id), SelectResult.property("content"))
            .from(DataSource.collection(testCollection))
            .where(FullTextFunction.match(idx, "sqlite database"))
            .orderBy(Ordering.property(TEST_DOC_SORT_KEY).ascending())
        verifyQuery(query, 1) { _, result -> assertEquals(mDoc3.id, result.getString("id")) }

        // OR operator
        query = QueryBuilder.select(SelectResult.expression(Meta.id), SelectResult.property("content"))
            .from(DataSource.collection(testCollection))
            .where(FullTextFunction.match(idx, "sqlite OR database"))
            .orderBy(Ordering.property(TEST_DOC_SORT_KEY).ascending())
        val expected = arrayOf(mDoc1.id, mDoc2.id, mDoc3.id)
        verifyQuery(query, 3) { n, result -> assertEquals(expected[n - 1], result.getString("id")) }

        // NOT operator
        query = QueryBuilder.select(SelectResult.expression(Meta.id), SelectResult.property("content"))
            .from(DataSource.collection(testCollection))
            .where(FullTextFunction.match(idx, "database NOT sqlite"))
            .orderBy(Ordering.property(TEST_DOC_SORT_KEY).ascending())
        verifyQuery(query, 1) { _, result -> assertEquals(mDoc1.id, result.getString("id")) }
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1621
    @Test
    fun testFTSMixedOperators() {
        val mDoc1 = MutableDocument()
        mDoc1.setString("content", "a database is a software system")
        mDoc1.setInt(TEST_DOC_SORT_KEY, 10)
        saveDocInCollection(mDoc1)

        val mDoc2 = MutableDocument()
        mDoc2.setString("content", "sqlite is a software system")
        mDoc2.setInt(TEST_DOC_SORT_KEY, 20)
        saveDocInCollection(mDoc2)

        val mDoc3 = MutableDocument()
        mDoc3.setString("content", "sqlite is a database")
        mDoc3.setInt(TEST_DOC_SORT_KEY, 30)
        saveDocInCollection(mDoc3)

        val ftsIndex = IndexBuilder.fullTextIndex(FullTextIndexItem.property("content"))
        testCollection.createIndex("ftsIndex", ftsIndex)
        val idx = Expression.fullTextIndex("ftsIndex")

        // The enhanced query syntax
        // https://www.sqlite.org/fts3.html#_set_operations_using_the_enhanced_query_syntax

        // A AND B AND C
        var query = QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.property("content"))
            .from(DataSource.collection(testCollection))
            .where(FullTextFunction.match(idx, "sqlite AND software AND system"))
            .orderBy(Ordering.property(TEST_DOC_SORT_KEY).ascending())

        verifyQuery(query, 1) { _, result -> assertEquals(mDoc2.id, result.getString("id")) }

        // (A AND B) OR C
        query = QueryBuilder.select(SelectResult.expression(Meta.id), SelectResult.property("content"))
            .from(DataSource.collection(testCollection))
            .where(FullTextFunction.match(idx, "(sqlite AND software) OR database"))
            .orderBy(Ordering.property(TEST_DOC_SORT_KEY).ascending())

        val expectedIDs2 = arrayOf(mDoc1.id, mDoc2.id, mDoc3.id)
        verifyQuery(query, 3) { n, result -> assertEquals(expectedIDs2[n - 1], result.getString("id")) }

        query = QueryBuilder.select(SelectResult.expression(Meta.id), SelectResult.property("content"))
            .from(DataSource.collection(testCollection))
            .where(FullTextFunction.match(idx, "(sqlite AND software) OR system"))
            .orderBy(Ordering.property(TEST_DOC_SORT_KEY).ascending())

        val expectedIDs3 = arrayOf(mDoc1.id, mDoc2.id)
        verifyQuery(query, 2) { n, result -> assertEquals(expectedIDs3[n - 1], result.getString("id")) }

        // (A OR B) AND C
        query = QueryBuilder.select(SelectResult.expression(Meta.id), SelectResult.property("content"))
            .from(DataSource.collection(testCollection))
            .where(FullTextFunction.match(idx, "(sqlite OR software) AND database"))
            .orderBy(Ordering.property(TEST_DOC_SORT_KEY).ascending())

        val expectedIDs4 = arrayOf(mDoc1.id, mDoc3.id)
        verifyQuery(query, 2) { n, result -> assertEquals(expectedIDs4[n - 1], result.getString("id")) }

        query = QueryBuilder.select(SelectResult.expression(Meta.id), SelectResult.property("content"))
            .from(DataSource.collection(testCollection))
            .where(FullTextFunction.match(idx, "(sqlite OR software) AND system"))
            .orderBy(Ordering.property(TEST_DOC_SORT_KEY).ascending())

        val expectedIDs5 = arrayOf(mDoc1.id, mDoc2.id)
        verifyQuery(query, 2) { n, result -> assertEquals(expectedIDs5[n - 1], result.getString("id")) }

        // A OR B OR C
        query = QueryBuilder.select(SelectResult.expression(Meta.id), SelectResult.property("content"))
            .from(DataSource.collection(testCollection))
            .where(FullTextFunction.match(idx, "database OR software OR system"))
            .orderBy(Ordering.property(TEST_DOC_SORT_KEY).ascending())

        val expectedIDs6 = arrayOf(mDoc1.id, mDoc2.id, mDoc3.id)
        verifyQuery(query, 3) { n, result -> assertEquals(expectedIDs6[n - 1], result.getString("id")) }
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1628
    @Test
    fun testLiveQueryResultsCount() = runBlocking {
        loadDocuments(50)

        val query= QueryBuilder
            .select()
            .from(DataSource.collection(testCollection))
            .where(Expression.property(TEST_DOC_SORT_KEY).greaterThan(Expression.intValue(25)))
            .orderBy(Ordering.property(TEST_DOC_SORT_KEY).ascending())

        val latch1 = CountDownLatch(1)
        val latch2 = CountDownLatch(1)

        val listener: QueryChangeListener = { change ->
            var count = 0
            val rs = change.results
            while (rs?.next() != null) {
                count++
                // The first run of this query should see a result set with 25 results:
                // 50 docs in the db minus 25 with values < 25
                // When we add 50 more documents, after the first latch springs,
                // there are 100 docs in the db, 75 of which have vaules > 25
                if ((count >= 25) && (latch1.getCount() > 0)) { latch1.countDown() }
                else if (count >= 75) { latch2.countDown() }
            }
        }

        query.addChangeListener(testSerialCoroutineContext, listener).use {
            assertTrue(latch1.await(LONG_TIMEOUT_SEC.seconds))

            loadDocuments(51, 50, testCollection)

            assertTrue(latch2.await(LONG_TIMEOUT_SEC.seconds))
        }
    }

    // https://forums.couchbase.com/t/
    //     how-to-be-notifed-that-document-is-changed-but-livequerys-query-isnt-catching-it-anymore/16199/9
    @Test
    fun testLiveQueryNotification() = runBlocking {
        // save doc1 with sort key = 5
        var doc = MutableDocument()
        doc.setInt(TEST_DOC_SORT_KEY, 5)
        saveDocInCollection(doc)

        val query= QueryBuilder.select(SelectResult.expression(Meta.id), SelectResult.property(TEST_DOC_SORT_KEY))
            .from(DataSource.collection(testCollection))
            .where(Expression.property(TEST_DOC_SORT_KEY).lessThan(Expression.intValue(10)))
            .orderBy(Ordering.property(TEST_DOC_SORT_KEY))

        val latch1 = CountDownLatch(1)
        val latch2 = CountDownLatch(1)

        val listener: QueryChangeListener = { change ->
            var matches = 0
            for (ignored: Result in change.results!!) { matches++ }

            // match doc1 with number1 -> 5 which is less than 10
            if (matches == 1) { latch1.countDown() }
            // Not match with doc1 because number1 -> 15 which does not match the query criteria
            else { latch2.countDown() }
        }

        query.addChangeListener(testSerialCoroutineContext, listener).use {
            assertTrue(latch1.await(STD_TIMEOUT_SEC.seconds))

            doc = testCollection.getDocument(doc.id)!!.toMutable()
            doc.setInt(TEST_DOC_SORT_KEY, 15)
            saveDocInCollection(doc)

            assertTrue(latch2.await(STD_TIMEOUT_SEC.seconds))
        }
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1689
    @Test
    fun testQueryAndNLikeOperators() {
        val mDoc1 = MutableDocument()
        mDoc1.setString("name", "food")
        mDoc1.setString("description", "bar")
        mDoc1.setInt(TEST_DOC_SORT_KEY, 10)
        saveDocInCollection(mDoc1)

        val mDoc2 = MutableDocument()
        mDoc2.setString("name", "foo")
        mDoc2.setString("description", "unknown")
        mDoc2.setInt(TEST_DOC_SORT_KEY, 20)
        saveDocInCollection(mDoc2)

        val mDoc3 = MutableDocument()
        mDoc3.setString("name", "water")
        mDoc3.setString("description", "drink")
        mDoc3.setInt(TEST_DOC_SORT_KEY, 30)
        saveDocInCollection(mDoc3)

        val mDoc4 = MutableDocument()
        mDoc4.setString("name", "chocolate")
        mDoc4.setString("description", "bar")
        mDoc4.setInt(TEST_DOC_SORT_KEY, 40)
        saveDocInCollection(mDoc4)

        // LIKE operator only
        var query= QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))
            .where(Expression.property("name").like(Expression.string("%foo%")))
            .orderBy(Ordering.property(TEST_DOC_SORT_KEY).ascending())

        verifyQuery(
            query,
            2
        ) { n, result ->
            assertEquals(1, result.count())
            if (n == 1) { assertEquals(mDoc1.id, result.getString(0)) }
            else { assertEquals(mDoc2.id, result.getString(0)) }
        }

        // EQUAL operator only
        query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))
            .where(Expression.property("description").equalTo(Expression.string("bar")))
            .orderBy(Ordering.property(TEST_DOC_SORT_KEY).ascending())

        verifyQuery(
            query,
            2
        ) { n, result ->
            assertEquals(1, result.count())
            if (n == 1) { assertEquals(mDoc1.id, result.getString(0)) }
            else { assertEquals(mDoc4.id, result.getString(0)) }
        }

        // AND and LIKE operators
        query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))
            .where(Expression.property("name").like(Expression.string("%foo%"))
                .and(Expression.property("description").equalTo(Expression.string("bar")))
            )
            .orderBy(Ordering.property(TEST_DOC_SORT_KEY).ascending())

        verifyQuery(
            query,
            1
        ) { _, result ->
            assertEquals(1, result.count())
            assertEquals(mDoc1.id, result.getString(0))
        }
    }

    // https://forums.couchbase.com/t/
    //     how-to-implement-an-index-join-clause-in-couchbase-lite-2-0-using-objective-c-api/16246
    // https://github.com/couchbase/couchbase-lite-core/issues/497
    @Test
    fun testQueryJoinAndSelectAll() {
        loadDocuments(100)

        val joinme = MutableDocument()
        joinme.setValue("theone", 42)
        saveDocInCollection(joinme)

        val query= QueryBuilder.select(SelectResult.all().from("main"), SelectResult.all().from("secondary"))
            .from(DataSource.collection(testCollection).`as`("main"))
            .join(Join.leftJoin(DataSource.collection(testCollection).`as`("secondary"))
                .on(Expression.property(TEST_DOC_SORT_KEY).from("main").equalTo(Expression.property("theone")
                    .from("secondary")
                ))
            )

        verifyQuery(
            query,
            101
        ) { n, result ->
            if (n == 41) {
                assertEquals(59, result.getDictionary("main")!!.getInt(TEST_DOC_REV_SORT_KEY))
                assertNull(result.getDictionary("secondary"))
            }
            if (n == 42) {
                assertEquals(58, result.getDictionary("main")!!.getInt(TEST_DOC_REV_SORT_KEY))
                assertEquals(42, result.getDictionary("secondary")!!.getInt("theone"))
            }
        }
    }

    @Test
    fun testResultSetAllResults() {
        val doc1a = MutableDocument()
        doc1a.setInt("answer", 42)
        doc1a.setString("a", "string")
        saveDocInCollection(doc1a)

        val query= QueryBuilder.select(SelectResult.expression(Meta.id), SelectResult.expression(Meta.deleted))
            .from(DataSource.collection(testCollection))
            .where(Meta.id.equalTo(Expression.string(doc1a.id)))

        query.execute().use { rs ->
            assertEquals(1, rs.allResults().size)
            assertEquals(0, rs.allResults().size)
        }
    }

//    @Test
//    fun testAggregateFunctionEmptyArgs() {
//        Function.count(null)
//
//        assertFailsWith<IllegalArgumentException> { Function.avg(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.min(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.max(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.sum(null) }
//    }
//
//    @Test
//    fun testMathFunctionEmptyArgs() {
//        assertFailsWith<IllegalArgumentException> { Function.abs(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.acos(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.asin(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.atan(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.atan2(null, Expression.doubleValue(0.7)) }
//
//        assertFailsWith<IllegalArgumentException> { Function.atan2(Expression.doubleValue(0.7), null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.ceil(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.cos(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.degrees(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.exp(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.floor(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.ln(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.log(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.power(null, Expression.intValue(2)) }
//
//        assertFailsWith<IllegalArgumentException> { Function.power(Expression.intValue(2), null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.radians(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.round(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.round(null, Expression.intValue(2)) }
//
//        assertFailsWith<IllegalArgumentException> { Function.round(Expression.doubleValue(0.567), null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.sign(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.sin(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.sqrt(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.tan(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.trunc(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.trunc(null, Expression.intValue(1)) }
//
//        assertFailsWith<IllegalArgumentException> { Function.trunc(Expression.doubleValue(79.15), null) }
//    }
//
//    @Test
//    fun testStringFunctionEmptyArgs() {
//        assertFailsWith<IllegalArgumentException> {
//            Function.contains(null, Expression.string("someSubString"))
//        }
//
//        assertFailsWith<IllegalArgumentException> {
//            Function.contains(Expression.string("somestring"), null)
//        }
//
//        assertFailsWith<IllegalArgumentException> { Function.length(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.lower(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.ltrim(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.rtrim(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.trim(null) }
//
//        assertFailsWith<IllegalArgumentException> { Function.upper(null) }
//    }

    @Test
    fun testStringToMillis() {
        createDateDocs()

        val expectedJST = listOf(
            null,
            499105260000L,
            499105290000L,
            499105290500L,
            499105290550L,
            499105290555L
        )

        val expectedPST = listOf(
            null,
            499166460000L,
            499166490000L,
            499166490500L,
            499166490550L,
            499166490555L
        )

        val expectedUTC = listOf(
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
        Report.log("Local time offset: $offset")
        val expectedLocal = mutableListOf(
            499132800000L - offset
        )
        var first = true
        for (entry in expectedUTC) {
            if (first) {
                first = false
                continue
            }
            expectedLocal.add(entry as Long - offset)
        }

        val query= QueryBuilder.select(
                SelectResult.expression(Function.stringToMillis(Expression.property("local"))),
                SelectResult.expression(Function.stringToMillis(Expression.property("JST"))),
                SelectResult.expression(Function.stringToMillis(Expression.property("JST2"))),
                SelectResult.expression(Function.stringToMillis(Expression.property("PST"))),
                SelectResult.expression(Function.stringToMillis(Expression.property("PST2"))),
                SelectResult.expression(Function.stringToMillis(Expression.property("UTC")))
            )
            .from(DataSource.collection(testCollection))
            .orderBy(Ordering.property("local").ascending())

        verifyQuery(
            query,
            6
        ) { n, result ->
            assertEquals(expectedLocal[n - 1], result.getNumber(0))
            assertEquals(expectedJST[n - 1], result.getNumber(1))
            assertEquals(expectedJST[n - 1], result.getNumber(2))
            assertEquals(expectedPST[n - 1], result.getNumber(3))
            assertEquals(expectedPST[n - 1], result.getNumber(4))
            assertEquals(expectedUTC[n - 1], result.getNumber(5))
        }
    }

    @Test
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

        val query= QueryBuilder.select(
                SelectResult.expression(Function.stringToUTC(Expression.property("local"))),
                SelectResult.expression(Function.stringToUTC(Expression.property("JST"))),
                SelectResult.expression(Function.stringToUTC(Expression.property("JST2"))),
                SelectResult.expression(Function.stringToUTC(Expression.property("PST"))),
                SelectResult.expression(Function.stringToUTC(Expression.property("PST2"))),
                SelectResult.expression(Function.stringToUTC(Expression.property("UTC")))
            )
            .from(DataSource.collection(testCollection))
            .orderBy(Ordering.property("local").ascending())

        verifyQuery(
            query,
            6
        ) { n, result ->
            assertEquals(expectedLocal[n - 1], result.getString(0))
            assertEquals(expectedJST[n - 1], result.getString(1))
            assertEquals(expectedJST[n - 1], result.getString(2))
            assertEquals(expectedPST[n - 1], result.getString(3))
            assertEquals(expectedPST[n - 1], result.getString(4))
            assertEquals(expectedUTC[n - 1], result.getString(5))
        }
    }

    @Test
    fun testMillisConversion() {
        val expectedUTC = listOf(
            "1985-10-26T00:00:00Z",
            "1985-10-26T01:21:00Z",
            "1985-10-26T01:21:30Z",
            "1985-10-26T01:21:30.500Z",
            "1985-10-26T01:21:30.550Z",
            "1985-10-26T01:21:30.555Z"
        )

        for (t in arrayOf(
            499132800000L,
            499137660000L,
            499137690000L,
            499137690500L,
            499137690550L,
            499137690555L
        )) {
            saveDocInCollection(MutableDocument().setNumber("timestamp", t))
        }

        val query= QueryBuilder.select(
                SelectResult.expression(Function.millisToString(Expression.property("timestamp"))),
                SelectResult.expression(Function.millisToUTC(Expression.property("timestamp")))
            )
            .from(DataSource.collection(testCollection))
            .orderBy(Ordering.property("timestamp").ascending())

        verifyQuery(
            query,
            6
        ) { n, result ->
            val i = n - 1
            assertEquals(expectedUTC[i], result.getString(1))
        }
    }

    @Test
    fun testQueryDocumentWithDollarSign() {
        saveDocInCollection(MutableDocument()
            .setString("\$type", "book")
            .setString("\$description", "about cats")
            .setString("\$price", "$100")
        )
        saveDocInCollection(MutableDocument()
            .setString("\$type", "book")
            .setString("\$description", "about dogs")
            .setString("\$price", "$95")
        )
        saveDocInCollection(MutableDocument()
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
            .from(DataSource.collection(testCollection))
            .where(Expression.property("\$type").equalTo(Expression.string("book")))

        q.execute().use { res ->
            for (r in res) {
                books++
                val p = r.getString("\$price")!!
                if (p.substring(1).toInt() < 100) { cheapBooks++ }
            }
            assertEquals(2, books)
            assertEquals(1, cheapBooks)
        }
    }

    @Test
    fun testN1QLSelect() {
        loadDocuments(100)

        val query = testDatabase.createQuery(
            "SELECT " + TEST_DOC_SORT_KEY + ", " + TEST_DOC_REV_SORT_KEY
                    + " FROM " + testCollection.fullName
        )

        verifyQuery(
            query,
            100
        ) { n, result ->
            assertEquals(n, result.getInt(TEST_DOC_SORT_KEY))
            assertEquals(n, result.getInt(0))
            assertEquals(100 - n, result.getInt(TEST_DOC_REV_SORT_KEY))
            assertEquals(100 - n, result.getInt(1))
        }
    }

    @Test
    fun testN1QLSelectStarFromDefault() {
        loadDocuments(100, testDatabase.defaultCollection)
        verifyQuery(
            testDatabase.createQuery("SELECT * FROM _default"),
            100
        ) { n, result ->
            assertEquals(1, result.count())
            val a1 = result.getDictionary(0)!!
            val a2 = result.getDictionary("_default")!!
            assertEquals(n, a1.getInt(TEST_DOC_SORT_KEY))
            assertEquals(100 - n, a1.getInt(TEST_DOC_REV_SORT_KEY))
            assertEquals(n, a2.getInt(TEST_DOC_SORT_KEY))
            assertEquals(100 - n, a2.getInt(TEST_DOC_REV_SORT_KEY))
        }
    }

    @Test
    fun testN1QLSelectStarFromCollection() {
        loadDocuments(100)

        verifyQuery(
            testDatabase.createQuery("SELECT * FROM " + testCollection.fullName),
            100
        ) { n, result ->
            assertEquals(1, result.count())
            val a1 = result.getDictionary(0)!!
            val a2 = result.getDictionary(testCollection.name)!!
            assertEquals(n, a1.getInt(TEST_DOC_SORT_KEY))
            assertEquals(100 - n, a1.getInt(TEST_DOC_REV_SORT_KEY))
            assertEquals(n, a2.getInt(TEST_DOC_SORT_KEY))
            assertEquals(100 - n, a2.getInt(TEST_DOC_REV_SORT_KEY))
        }
    }

    @Test
    fun testN1QLSelectStarFromUnderscore() {
        loadDocuments(100, testDatabase.defaultCollection)
        verifyQuery(
            testDatabase.createQuery("SELECT * FROM _"),
            100
        ) { n, result ->
            assertEquals(1, result.count())
            val a1 = result.getDictionary(0)!!
            val a2 = result.getDictionary("_")!!
            assertEquals(n, a1.getInt(TEST_DOC_SORT_KEY))
            assertEquals(100 - n, a1.getInt(TEST_DOC_REV_SORT_KEY))
            assertEquals(n, a2.getInt(TEST_DOC_SORT_KEY))
            assertEquals(100 - n, a2.getInt(TEST_DOC_REV_SORT_KEY))
        }
    }

//    @Test
//    fun testWhereNullOrMissing() {
//        val doc1 = MutableDocument()
//        doc1.setValue("name", "Scott")
//        doc1.setValue("address", null)
//        saveDocInCollection(doc1)
//
//        val doc2 = MutableDocument()
//        doc2.setValue("name", "Tiger")
//        doc2.setValue("address", "123 1st ave.")
//        doc2.setValue("age", 20)
//        saveDocInCollection(doc2)
//
//        val name = Expression.property("name")
//        val address = Expression.property("address")
//        val age = Expression.property("age")
//        val work = Expression.property("work")
//
//        for (testCase in arrayOf(
//            TestCase(name.isNullOrMissing()),
//            TestCase(name.notNullOrMissing(), doc1.id, doc2.id),
//            TestCase(address.isNullOrMissing(), doc1.id),
//            TestCase(address.notNullOrMissing(), doc2.id),
//            TestCase(age.isNullOrMissing(), doc1.id),
//            TestCase(age.notNullOrMissing(), doc2.id),
//            TestCase(work.isNullOrMissing(), doc1.id, doc2.id),
//            TestCase(work.notNullOrMissing())
//        )) {
//            verifyQuery(
//                QueryBuilder.select(SelectResult.expression(Meta.id))
//                    .from(DataSource.collection(testCollection))
//                    .where(testCase.expr),
//                testCase.docIds.size) { n, result ->
//                if (n <= testCase.docIds.size) {
//                    assertEquals(testCase.docIds[n - 1], result.getString(0))
//                }
//            }
//        }
//    }

    @Suppress("DEPRECATION")
    @Test
    fun testLegacyIndexMatch() {
        loadJSONResourceIntoCollection("sentences.json")

        testCollection.createIndex("sentence", IndexBuilder.fullTextIndex(FullTextIndexItem.property("sentence")))

        val query= QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.property("sentence"))
            .from(DataSource.collection(testCollection))
            .where(FullTextFunction.match("sentence", "'Dummie woman'"))
            .orderBy(Ordering.expression(FullTextFunction.rank("sentence")).descending())

        verifyQuery(
            query,
            2
        ) { _, result ->
            assertNotNull(result.getString(0))
            assertNotNull(result.getString(1))
        }
    }

    @Test
    fun testConcurrentCreateAndQuery() = runBlocking {
        val latch1 = CountDownLatch(1) // 2nd thread waits for first to enter inBatch
        val latch2 = CountDownLatch(1) // 1st thread waits for 2nd to run a query: should time out
        val latch3 = CountDownLatch(2) // test is complete

        val n = AtomicInt(0) // ensure strict ordering of events
        val timeout = AtomicBoolean(false) // latch 2 should time out in the first thread
        val err = AtomicReference<Exception?>(null) // to capture any exceptions

        val t1 = async(Dispatchers.Default) {
            try {
                testDatabase.inBatch {
                    // the other thread should be waiting on the first latch
                    n.compareAndSet(0, 1)
                    latch1.countDown() // let the other thread run its query
                    runBlocking {
                        timeout.store(!latch2.await(1.seconds)) // this should time out
                    }
                    // the other thread should be past the first latch but should not have been able to start its query
                    n.compareAndSet(2, 3)
                }
            }
            catch (e: Exception) { err.compareAndSet(null, e) }
            finally { latch3.countDown() }
        }

        val t2 = async(Dispatchers.Default) {
            try {
                latch1.await()
                // this thread is allowed to run its query only after the other thread is in inBatch
                n.compareAndSet(1, 2)
                // this thread should not be able to run the query until the other thread has left inBatch
                try {
                    testDatabase.createQuery("SELECT * FROM _").execute().use {
                        // This latch should already have timed out in the other thread
                        latch2.countDown()
                        // shouldn't get here until the other thread has left inBatch
                        n.compareAndSet(3, 4)
                    }
                }
                catch (e: CouchbaseLiteException) { err.compareAndSet(null, e) }
            }
            catch (_: CancellationException) { }
            finally { latch3.countDown() }
        }

        t1.start()
        t2.start()

        assertTrue(latch3.await(STD_TIMEOUT_SEC.seconds))

        val e = err.load()
        assertEquals(4, n.load(), "Events did not occur in expected order")
        assertTrue(timeout.load(), "Latch 2 should have timed out")
        if (e != null) {
            // Cause isn't logged on native platforms...
            // https://youtrack.jetbrains.com/issue/KT-62794
            println("Cause:")
            println(e.message)
            println(e.stackTraceToString())
            throw AssertionError("Operation failed", e)
        }
    }

    // Utility Functions

    private fun runTests(vararg cases: TestCase) {
        for (testCase in cases) {
            val docIdList = testCase.docIds.toMutableList()
            val query = QueryBuilder.select(SelectResult.expression(Meta.id))
                    .from(DataSource.collection(testCollection))
                    .where(testCase.expr)
            verifyQuery(
                query,
                testCase.docIds.size
            ) { _, result -> docIdList.remove(result.getString(0)) }

            assertEquals(0, docIdList.size.toLong())
        }
    }

    private fun testOrdered(ordering: Ordering, cmp: Comparator<String>) {
        val firstNames = mutableListOf<String>()
        val numRows = verifyQueryWithEnumerator(
            QueryBuilder.select(SelectResult.expression(Meta.id))
                .from(DataSource.collection(testCollection))
                .orderBy(ordering)
        ) { _, result ->
            val docID = result.getString(0)
            val doc = testCollection.getDocument(docID!!)!!
            val name = doc.getDictionary("name")!!.toMap()
            val firstName = name["first"] as String
            firstNames.add(firstName)
        }
        assertEquals(100, numRows.toLong())
        assertEquals(100, firstNames.size.toLong())

        val sorted = firstNames.sortedWith(cmp)
        assertContentEquals(sorted, firstNames)
    }

    private fun createAlphaDocs() {
        for (letter in arrayOf("B", "Z", "Å", "A")) {
            val doc = MutableDocument()
            doc.setValue("string", letter)
            saveDocInCollection(doc)
        }
    }

    private fun createDateDocs() {
        var doc = MutableDocument()
        doc.setString("local", "1985-10-26")
        saveDocInCollection(doc)

        for (format in arrayOf(
            "1985-10-26 01:21",
            "1985-10-26 01:21:30",
            "1985-10-26 01:21:30.5",
            "1985-10-26 01:21:30.55",
            "1985-10-26 01:21:30.555"
        )) {
            doc = MutableDocument()
            doc.setString("local", format)
            doc.setString("JST", "$format+09:00")
            doc.setString("JST2", "$format+0900")
            doc.setString("PST", "$format-08:00")
            doc.setString("PST2", "$format-0800")
            doc.setString("UTC", format + "Z")
            saveDocInCollection(doc)
        }
    }

    private fun jsonDocId(i: Int): String =
        "doc-${i.paddedString(3)}"

    private fun createTaskDocument(title: String, complete: Boolean): Document {
        val doc = MutableDocument()
        doc.setString("type", "task")
        doc.setString("title", title)
        doc.setBoolean("complete", complete)
        return saveDocInCollection(doc)
    }

    private suspend fun liveQueryNoUpdate(test: (QueryChange) -> Unit) {
        loadDocuments(100)

        val query: Query = QueryBuilder
            .select(SelectResult.expression(Expression.property(TEST_DOC_SORT_KEY)))
            .from(DataSource.collection(testCollection))
            .where(Expression.property(TEST_DOC_SORT_KEY).lessThan(Expression.intValue(50)))
            .orderBy(Ordering.property(TEST_DOC_SORT_KEY).ascending())

        val latch1 = CountDownLatch(1)
        val latch2 = CountDownLatch(1)
        val listener: QueryChangeListener = { change ->
            test(change)
            // Attaching the listener should run the query and get the results
            // That will pop the latch allowing the addition of a bunch more docs
            // Those new docs, however, do not fit the where clause and should
            // not cause the listener to be called again.
            if (latch1.getCount() > 0) {
                latch1.countDown()
            } else {
                latch2.countDown()
            }
        }

        query.addChangeListener(testSerialCoroutineContext, listener).use {
            assertTrue(latch1.await(STD_TIMEOUT_SEC.seconds))
            // create more docs
            loadDocuments(101, 100)

            // Wait 5 seconds
            // The latch should not pop, because the listener should be called only once
            // ??? This is a very expensive way to test
            assertFalse(latch2.await(5.seconds))
            assertEquals(1, latch2.getCount())
        }
    }
}

internal expect fun localToUTC(format: String, dateStr: String): String
