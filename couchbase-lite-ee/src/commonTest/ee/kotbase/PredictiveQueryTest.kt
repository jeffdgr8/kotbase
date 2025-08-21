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
package kotbase

import kotbase.ext.nowMillis
import kotbase.ext.toStringMillis
import kotbase.test.IgnoreLinuxMingw
import kotbase.test.assertIntContentEquals
import kotlin.time.Clock
import kotlin.test.*

class PredictiveQueryTest : BaseQueryTest() {

    // PredictiveQueryTest.swift

    override fun setUpLast() {
        Database.prediction.unregisterModel(AggregateModel.NAME)
        Database.prediction.unregisterModel(TextModel.NAME)
        Database.prediction.unregisterModel(EchoModel.NAME)
    }

    private fun createDocument(numbers: List<Int>): MutableDocument {
        val doc = MutableDocument()
        doc.setValue("numbers", numbers)
        testCollection.save(doc)
        return doc
    }

    private fun blobForString(text: String): Blob =
        Blob("text/plain", text.encodeToByteArray())

    @Test
    fun testRegisterAndUnregisterModel() {
        createDocument(listOf(1, 2, 3, 4, 5))

        val model = AggregateModel.NAME
        val input = Expression.value(mapOf("numbers" to Expression.property("numbers")))
        val prediction = Function.prediction(model, input)
        val q = QueryBuilder
            .select(SelectResult.expression(prediction))
            .from(DataSource.collection(testCollection))

        // Query before registering the model:
        assertThrowsCBLException(CBLError.Domain.SQLITE, 1) {
            q.execute()
        }

        val aggregateModel = AggregateModel()
        aggregateModel.registerModel()

        verifyQuery(q, 1) { _, r ->
            val pred = r.getDictionary(0)!!
            assertEquals(15, pred.getInt("sum"))
        }

        aggregateModel.unregisterModel()

        // Query after unregistering the model:
        assertThrowsCBLException(CBLError.Domain.SQLITE, 1) {
            q.execute()
        }
    }

    @Test
    fun testRegisterMultipleModelsWithSameName() {
        createDocument(listOf(1, 2, 3, 4, 5))

        val model = "TheModel"
        val aggregateModel = AggregateModel()
        Database.prediction.registerModel(model, aggregateModel)

        val input = Expression.value(mapOf("numbers" to Expression.property("numbers")))
        val prediction = Function.prediction(model, input)
        val q = QueryBuilder
            .select(SelectResult.expression(prediction))
            .from(DataSource.collection(testCollection))
        verifyQuery(q, 1) { _, r ->
            val pred = r.getDictionary(0)!!
            assertEquals(15, pred.getInt("sum"))
        }

        // Register a new model with the same name:
        val echoModel = EchoModel()
        Database.prediction.registerModel(model, echoModel)

        // Query again should use the new model:
        verifyQuery(q, 1) { _, r ->
            val pred = r.getDictionary(0)!!
            assertNull(pred.getValue("sum"))
            assertIntContentEquals(listOf(1, 2, 3, 4, 5), pred.getArray("numbers")!!.toList())
        }

        Database.prediction.unregisterModel(model)
    }

    @Test
    fun testPredictionInputOutput() {
        // Register echo model:
        val echoModel = EchoModel()
        echoModel.registerModel()

        // Create a doc:
        val doc = MutableDocument()
        doc.setString("name", "Daniel")
        doc.setInt("number", 2)
        saveDocInCollection(doc)

        // Create prediction function input:
        val date = Clock.System.nowMillis()
        val dateStr = date.toStringMillis()
        val power = Function.power(Expression.property("number"), Expression.value(2))
        val dict = mapOf(
            // Literal:
            "number1" to 10,
            "number2" to 10.1,
            "int_min" to Int.MIN_VALUE,
            "int_max" to Int.MAX_VALUE,
            "long_min" to Long.MIN_VALUE,
            "long_max" to Long.MAX_VALUE,
            "float_min" to Float.MIN_VALUE,
            "float_max" to Float.MAX_VALUE,
            "double_min" to Double.MIN_VALUE,
            // rounding error: https://issues.couchbase.com/browse/CBL-1363
            // "double_max" to Double.MAX_VALUE,
            "boolean_true" to true,
            "boolean_false" to false,
            "string" to "hello",
            "date" to date,
            "null" to null,
            "dict" to mapOf("foo" to "bar"),
            "array" to listOf("1", "2", "3"),
            // Expression:
            "expr_property" to Expression.property("name"),
            "expr_value_number1" to Expression.value(20),
            "expr_value_number2" to Expression.value(20.1),
            "expr_value_boolean" to Expression.value(true),
            "expr_value_string" to Expression.value("hi"),
            "expr_value_date" to Expression.value(date),
            "expr_value_null" to Expression.value(null),
            "expr_value_dict" to Expression.value(mapOf("ping" to "pong")),
            "expr_value_array" to Expression.value(listOf("4", "5", "6")),
            "expr_power" to power
        )

        // Execute query and validate output:
        val input = Expression.value(dict)
        val model = EchoModel.NAME
        val prediction = Function.prediction(model, input)
        val q = QueryBuilder
            .select(SelectResult.expression(prediction))
            .from(DataSource.collection(testCollection))
        verifyQuery(q, 1) { _, r ->
            val pred = r.getDictionary(0)!!
            assertEquals(dict.size, pred.count)
            // Literal:
            assertEquals(10, pred.getInt("number1"))
            assertEquals(10.1, pred.getDouble("number2"))
            assertEquals(Int.MIN_VALUE, pred.getInt("int_min"))
            assertEquals(Int.MAX_VALUE, pred.getInt("int_max"))
            assertEquals(Long.MIN_VALUE, pred.getLong("long_min"))
            assertEquals(Long.MAX_VALUE, pred.getLong("long_max"))
            assertEquals(Float.MIN_VALUE, pred.getFloat("float_min"))
            assertEquals(Float.MAX_VALUE, pred.getFloat("float_max"))
            assertEquals(Double.MIN_VALUE, pred.getDouble("double_min"))
            assertEquals(true, pred.getBoolean("boolean_true"))
            assertEquals(false, pred.getBoolean("boolean_false"))
            assertEquals("hello", pred.getString("string"))
            assertEquals(dateStr, pred.getDate("date")!!.toStringMillis())
            assertEquals(null, pred.getValue("null"))
            assertEquals(mapOf("foo" to "bar"), pred.getDictionary("dict")!!.toMap())
            assertEquals(listOf("1", "2", "3"), pred.getArray("array")!!.toList())
            // Expression:
            assertEquals("Daniel", pred.getString("expr_property"))
            assertEquals(20, pred.getInt("expr_value_number1"))
            assertEquals(20.1, pred.getDouble("expr_value_number2"))
            assertEquals(true, pred.getBoolean("expr_value_boolean"))
            assertEquals("hi", pred.getString("expr_value_string"))
            assertEquals(dateStr, pred.getDate("expr_value_date")!!.toStringMillis())
            assertEquals(null, pred.getValue("expr_value_null"))
            assertEquals(mapOf("ping" to "pong"), pred.getDictionary("expr_value_dict")!!.toMap())
            assertEquals(listOf("4", "5", "6"), pred.getArray("expr_value_array")!!.toList())
            assertEquals(4, pred.getInt("expr_power"))
        }

        echoModel.unregisterModel()
    }

    // crashes during query in linuxMingw
    @IgnoreLinuxMingw
    @Test
    fun testPredictionWithBlobPropertyInput() {
        val texts = listOf(
            "Knox on fox in socks in box. Socks on Knox and Knox in box.",
            "Clocks on fox tick. Clocks on Knox tock. Six sick bricks tick. Six sick chicks tock."
        )

        for (text in texts) {
            val doc = MutableDocument()
            doc.setBlob("text", blobForString(text))
            testCollection.save(doc)
        }

        val textModel = TextModel()
        textModel.registerModel()

        val model = TextModel.NAME
        val input = Expression.map(mapOf("text" to Expression.property("text")))
        val prediction = Function.prediction(model, input)

        val q = QueryBuilder
            .select(
                SelectResult.property("text"),
                SelectResult.expression(prediction.propertyPath("wc")).`as`("wc")
            ).from(DataSource.collection(testCollection))
            .where(prediction.propertyPath("wc").greaterThan(Expression.value(15)))

        verifyQuery(q, 1) { _, r ->
            val blob = r.getBlob("text")!!
            val text = blob.content!!.decodeToString()
            assertEquals(texts[1], text)
            assertEquals(16, r.getInt("wc"))
        }

        textModel.unregisterModel()
    }

    // crashes during query in linuxMingw
    @IgnoreLinuxMingw
    @Test
    fun testPredictionWithBlobParameterInput() {
        testCollection.save(MutableDocument())

        val textModel = TextModel()
        textModel.registerModel()

        val model = TextModel.NAME
        val input = Expression.map(mapOf("text" to Expression.parameter("text")))
        val prediction = Function.prediction(model, input)

        val q = QueryBuilder
            .select(SelectResult.expression(prediction.propertyPath("wc")).`as`("wc"))
            .from(DataSource.collection(testCollection))

        val params = Parameters()
        params.setBlob(
            "text",
            blobForString("Knox on fox in socks in box. Socks on Knox and Knox in box.")
        )
        q.parameters = params

        verifyQuery(q, 1) { _, r ->
            assertEquals(14, r.getInt(0))
        }

        textModel.unregisterModel()
    }

    @Test
    fun testPredictionWithNonSupportedInputTypes() {
        testCollection.save(MutableDocument())

        val echoModel = EchoModel()
        echoModel.registerModel()

        // Query with non dictionary input:
        val model = EchoModel.NAME
        val input = Expression.value("string")
        val prediction = Function.prediction(model, input)
        val q = QueryBuilder
            .select(SelectResult.expression(prediction))
            .from(DataSource.collection(testCollection))

        assertThrowsCBLException(CBLError.Domain.SQLITE, 1) {
            q.execute()
        }

        // Query with non-supported value type in dictionary input.
        // Note: The code below will crash the test for Apple as Kotlin
        // cannot handle exception thrown by Objective-C
        //
        //assertFailsWith<IllegalArgumentException> {
        //    val input2 = Expression.value(mapOf("key" to this))
        //    val prediction2 = Function.prediction(model, input2)
        //    val q2 = QueryBuilder
        //        .select(SelectResult.expression(prediction2))
        //        .from(DataSource.collection(testCollection))
        //    q2.execute()
        //}

        echoModel.unregisterModel()
    }

    @Test
    fun testQueryPredictionResultDictionary() {
        createDocument(listOf(1, 2, 3, 4, 5))
        createDocument(listOf(6, 7, 8, 9, 10))

        val aggregateModel = AggregateModel()
        aggregateModel.registerModel()

        val model = AggregateModel.NAME
        val input = Expression.value(mapOf("numbers" to Expression.property("numbers")))
        val prediction = Function.prediction(model, input)
        val q = QueryBuilder
            .select(SelectResult.property("numbers"), SelectResult.expression(prediction))
            .from(DataSource.collection(testCollection))

        verifyQuery(q, 2) { _, r ->
            @Suppress("UNCHECKED_CAST")
            val numbers = r.getArray(0)!!.toList() as List<Int>
            assertTrue(numbers.isNotEmpty())

            val pred = r.getDictionary(1)!!
            assertEquals(numbers.sum(), pred.getInt("sum"))
            assertEquals(numbers.min().toInt(), pred.getInt("min"))
            assertEquals(numbers.max().toInt(), pred.getInt("max"))
            assertEquals(numbers.average().toInt(), pred.getInt("avg"))
        }

        aggregateModel.unregisterModel()
    }

    @Test
    fun testQueryPredictionValues() {
        createDocument(listOf(1, 2, 3, 4, 5))
        createDocument(listOf(6, 7, 8, 9, 10))

        val aggregateModel = AggregateModel()
        aggregateModel.registerModel()

        val model = AggregateModel.NAME
        val input = Expression.value(mapOf("numbers" to Expression.property("numbers")))
        val prediction = Function.prediction(model, input)
        val q = QueryBuilder
            .select(
                SelectResult.property("numbers"),
                SelectResult.expression(prediction.propertyPath("sum")).`as`("sum"),
                SelectResult.expression(prediction.propertyPath("min")).`as`("min"),
                SelectResult.expression(prediction.propertyPath("max")).`as`("max"),
                SelectResult.expression(prediction.propertyPath("avg")).`as`("avg")
            ).from(DataSource.collection(testCollection))

        verifyQuery(q, 2) { _, r ->
            @Suppress("UNCHECKED_CAST")
            val numbers = r.getArray(0)!!.toList() as List<Int>
            assertTrue(numbers.isNotEmpty())

            val sum = r.getInt(1)
            val min = r.getInt(2)
            val max = r.getInt(3)
            val avg = r.getInt(4)

            assertEquals(sum, r.getInt("sum"))
            assertEquals(min, r.getInt("min"))
            assertEquals(max, r.getInt("max"))
            assertEquals(avg, r.getInt("avg"))

            assertEquals(numbers.sum(), sum)
            assertEquals(numbers.min().toInt(), min)
            assertEquals(numbers.max().toInt(), max)
            assertEquals(numbers.average().toInt(), avg)
        }

        aggregateModel.unregisterModel()
    }

    @Test
    fun testWhereUsingPredictionValues() {
        createDocument(listOf(1, 2, 3, 4, 5))
        createDocument(listOf(6, 7, 8, 9, 10))

        val aggregateModel = AggregateModel()
        aggregateModel.registerModel()

        val model = AggregateModel.NAME
        val input = Expression.value(mapOf("numbers" to Expression.property("numbers")))
        val prediction = Function.prediction(model, input)
        val q = QueryBuilder
            .select(
                SelectResult.property("numbers"),
                SelectResult.expression(prediction.propertyPath("sum")).`as`("sum"),
                SelectResult.expression(prediction.propertyPath("min")).`as`("min"),
                SelectResult.expression(prediction.propertyPath("max")).`as`("max"),
                SelectResult.expression(prediction.propertyPath("avg")).`as`("avg")
            ).from(DataSource.collection(testCollection))
            .where(prediction.propertyPath("sum").equalTo(Expression.value(15)))

        verifyQuery(q, 1) { _, r ->
            @Suppress("UNCHECKED_CAST")
            val numbers = r.getArray(0)!!.toList() as List<Int>
            assertTrue(numbers.isNotEmpty())

            val sum = r.getInt(1)
            assertEquals(sum, 15)

            val min = r.getInt(2)
            val max = r.getInt(3)
            val avg = r.getInt(4)

            assertEquals(sum, r.getInt("sum"))
            assertEquals(min, r.getInt("min"))
            assertEquals(max, r.getInt("max"))
            assertEquals(avg, r.getInt("avg"))

            assertEquals(numbers.sum(), sum)
            assertEquals(numbers.min().toInt(), min)
            assertEquals(numbers.max().toInt(), max)
            assertEquals(numbers.average().toInt(), avg)
        }

        aggregateModel.unregisterModel()
    }

    @Test
    fun testOrderByUsingPredictionValues() {
        createDocument(listOf(1, 2, 3, 4, 5))
        createDocument(listOf(6, 7, 8, 9, 10))

        val aggregateModel = AggregateModel()
        aggregateModel.registerModel()

        val model = AggregateModel.NAME
        val input = Expression.value(mapOf("numbers" to Expression.property("numbers")))
        val prediction = Function.prediction(model, input)
        val q = QueryBuilder
            .select(SelectResult.expression(prediction.propertyPath("sum")).`as`("sum"))
            .from(DataSource.collection(testCollection))
            .where(prediction.propertyPath("sum").greaterThan(Expression.value(1)))
            .orderBy(Ordering.expression(prediction.propertyPath("sum")).descending())

        val sums = mutableListOf<Int>()
        val rows = verifyQueryWithEnumerator(q) { _, r ->
            sums.add(r.getInt(0))
        }
        assertEquals(2, rows)
        assertEquals(listOf(40, 15), sums)

        aggregateModel.unregisterModel()
    }

    @Test
    fun testPredictiveModelReturningNull() {
        createDocument(listOf(1, 2, 3, 4, 5))

        val doc = MutableDocument()
        doc.setString("text", "Knox on fox in socks in box. Socks on Knox and Knox in box.")
        saveDocInCollection(doc)

        val aggregateModel = AggregateModel()
        aggregateModel.registerModel()

        val model = AggregateModel.NAME
        val input = Expression.value(mapOf("numbers" to Expression.property("numbers")))
        val prediction = Function.prediction(model, input)
        var q: Query = QueryBuilder
            .select(
                SelectResult.expression(prediction),
                SelectResult.expression(prediction.propertyPath("sum")).`as`("sum")
            ).from(DataSource.collection(testCollection))

        verifyQuery(q, 2) { n, r ->
            if (n == 1) {
                assertNotNull(r.getDictionary(0))
                assertEquals(15, r.getInt(1))
            } else {
                assertNull(r.getValue(0))
                assertNull(r.getValue(1))
            }
        }

        // Evaluate with nullOrMissing:
        q = QueryBuilder
            .select(
                SelectResult.expression(prediction),
                SelectResult.expression(prediction.propertyPath("sum")).`as`("sum")
            ).from(DataSource.collection(testCollection))
            .where(prediction.isValued())

        verifyQuery(q, 1) { _, r ->
            assertNotNull(r.getDictionary(0))
            assertEquals(15, r.getInt(1))
        }
    }

    @Test
    fun testIndexPredictionValueUsingValueIndex() {
        createDocument(listOf(1, 2, 3, 4, 5))
        createDocument(listOf(6, 7, 8, 9, 10))

        val aggregateModel = AggregateModel()
        aggregateModel.registerModel()

        val model = AggregateModel.NAME
        val input = Expression.value(mapOf("numbers" to Expression.property("numbers")))
        val prediction = Function.prediction(model, input)

        val index = IndexBuilder.valueIndex(ValueIndexItem.expression(prediction.propertyPath("sum")))
        testCollection.createIndex("SumIndex", index)

        val q = QueryBuilder
            .select(SelectResult.property("numbers"), SelectResult.expression(prediction.propertyPath("sum")).`as`("sum"))
            .from(DataSource.collection(testCollection))
            .where(prediction.propertyPath("sum").equalTo(Expression.value(15)))

        val explain = q.explain()
        assertNotEquals(-1, explain.indexOf("USING INDEX SumIndex"))

        verifyQuery(q, 1) { _, r ->
            @Suppress("UNCHECKED_CAST")
            val numbers = r.getArray(0)!!.toList() as List<Int>
            assertTrue(numbers.isNotEmpty())

            val sum = r.getInt(1)
            assertEquals(numbers.sum(), sum)
        }
        assertEquals(2, aggregateModel.numberOfCalls)
    }

    @Test
    fun testIndexMultiplePredictionValuesUsingValueIndex() {
        createDocument(listOf(1, 2, 3, 4, 5))
        createDocument(listOf(6, 7, 8, 9, 10))

        val aggregateModel = AggregateModel()
        aggregateModel.registerModel()

        val model = AggregateModel.NAME
        val input = Expression.value(mapOf("numbers" to Expression.property("numbers")))
        val prediction = Function.prediction(model, input)

        val sumIndex = IndexBuilder.valueIndex(ValueIndexItem.expression(prediction.propertyPath("sum")))
        testCollection.createIndex("SumIndex", sumIndex)

        val avgIndex = IndexBuilder.valueIndex(ValueIndexItem.expression(prediction.propertyPath("avg")))
        testCollection.createIndex("AvgIndex", avgIndex)

        val q = QueryBuilder
            .select(
                SelectResult.expression(prediction.propertyPath("sum")).`as`("sum"),
                SelectResult.expression(prediction.propertyPath("avg")).`as`("avg")
            ).from(DataSource.collection(testCollection))
            .where(
                prediction.propertyPath("sum").lessThanOrEqualTo(Expression.value(15)).or(
                    prediction.propertyPath("avg").equalTo(Expression.value(8))
                )
            )

        val explain = q.explain()
        assertNotEquals(-1, explain.indexOf("USING INDEX SumIndex"))
        assertNotEquals(-1, explain.indexOf("USING INDEX AvgIndex"))

        verifyQuery(q, 2) { _, r ->
            assertTrue(r.getInt(0) == 15 || r.getInt(1) == 8)
        }
    }

    @Test
    fun testIndexCompoundPredictiveValuesUsingValueIndex() {
        createDocument(listOf(1, 2, 3, 4, 5))
        createDocument(listOf(6, 7, 8, 9, 10))

        val aggregateModel = AggregateModel()
        aggregateModel.registerModel()

        val model = AggregateModel.NAME
        val input = Expression.value(mapOf("numbers" to Expression.property("numbers")))
        val prediction = Function.prediction(model, input)

        val index = IndexBuilder.valueIndex(
            ValueIndexItem.expression(prediction.propertyPath("sum")),
            ValueIndexItem.expression(prediction.propertyPath("avg"))
        )
        testCollection.createIndex("SumAvgIndex", index)

        val q = QueryBuilder
            .select(
                SelectResult.expression(prediction.propertyPath("sum")).`as`("sum"),
                SelectResult.expression(prediction.propertyPath("avg")).`as`("avg")
            ).from(DataSource.collection(testCollection))
            .where(
                prediction.propertyPath("sum").equalTo(Expression.value(15)).and(
                    prediction.propertyPath("avg").equalTo(Expression.value(3))
                )
            )

        val explain = q.explain()
        assertNotEquals(-1, explain.indexOf("USING INDEX SumAvgIndex"))

        verifyQuery(q, 1) { _, r ->
            assertEquals(15, r.getInt(0))
            assertEquals(3, r.getInt(1))
        }
        assertEquals(4, aggregateModel.numberOfCalls)
    }

    // predictive indexes are not supported in CBL C SDK
    @IgnoreLinuxMingw
    @Test
    fun testIndexPredictionResultUsingPredictiveIndex() {
        createDocument(listOf(1, 2, 3, 4, 5))
        createDocument(listOf(6, 7, 8, 9, 10))

        val aggregateModel = AggregateModel()
        aggregateModel.registerModel()

        val model = AggregateModel.NAME
        val input = Expression.value(mapOf("numbers" to Expression.property("numbers")))
        val prediction = Function.prediction(model, input)

        val index = IndexBuilder.predictiveIndex(model, input)
        testCollection.createIndex("AggIndex", index)

        val q = QueryBuilder
            .select(
                SelectResult.property("numbers"),
                SelectResult.expression(prediction.propertyPath("sum")).`as`("sum")
            ).from(DataSource.collection(testCollection))
            .where(prediction.propertyPath("sum").equalTo(Expression.value(15)))

        val explain = q.explain()
        assertEquals(-1, explain.indexOf("USING INDEX AggIndex"))

        verifyQuery(q, 1) { _, r ->
            @Suppress("UNCHECKED_CAST")
            val numbers = r.getArray(0)!!.toList() as List<Int>
            assertTrue(numbers.isNotEmpty())

            val sum = r.getInt(1)
            assertEquals(numbers.sum(), sum)
        }
        assertEquals(2, aggregateModel.numberOfCalls)

        aggregateModel.unregisterModel()
    }

    // predictive indexes are not supported in CBL C SDK
    @IgnoreLinuxMingw
    @Test
    fun testIndexPredictionValueUsingPredictiveIndex() {
        createDocument(listOf(1, 2, 3, 4, 5))
        createDocument(listOf(6, 7, 8, 9, 10))

        val aggregateModel = AggregateModel()
        aggregateModel.registerModel()

        val model = AggregateModel.NAME
        val input = Expression.value(mapOf("numbers" to Expression.property("numbers")))
        val prediction = Function.prediction(model, input)

        val index = IndexBuilder.predictiveIndex(model, input, listOf("sum"))
        testCollection.createIndex("SumIndex", index)

        val q = QueryBuilder
            .select(
                SelectResult.property("numbers"),
                SelectResult.expression(prediction.propertyPath("sum")).`as`("sum")
            ).from(DataSource.collection(testCollection))
            .where(prediction.propertyPath("sum").equalTo(Expression.value(15)))

        val explain = q.explain()
        assertNotEquals(-1, explain.indexOf("USING INDEX SumIndex"))

        verifyQuery(q, 1) { _, r ->
            @Suppress("UNCHECKED_CAST")
            val numbers = r.getArray(0)!!.toList() as List<Int>
            assertTrue(numbers.isNotEmpty())

            val sum = r.getInt(1)
            assertEquals(numbers.sum(), sum)
        }
        assertEquals(2, aggregateModel.numberOfCalls)

        aggregateModel.unregisterModel()
    }

    // predictive indexes are not supported in CBL C SDK
    @IgnoreLinuxMingw
    @Test
    fun testIndexMultiplePredictionValuesUsingPredictiveIndex() {
        createDocument(listOf(1, 2, 3, 4, 5))
        createDocument(listOf(6, 7, 8, 9, 10))

        val aggregateModel = AggregateModel()
        aggregateModel.registerModel()

        val model = AggregateModel.NAME
        val input = Expression.value(mapOf("numbers" to Expression.property("numbers")))
        val prediction = Function.prediction(model, input)

        val sumIndex = IndexBuilder.predictiveIndex(model, input, listOf("sum"))
        testCollection.createIndex("SumIndex", sumIndex)

        val avgIndex = IndexBuilder.predictiveIndex(model, input, listOf("avg"))
        testCollection.createIndex("AvgIndex", avgIndex)

        val q = QueryBuilder
            .select(
                SelectResult.expression(prediction.propertyPath("sum")).`as`("sum"),
                SelectResult.expression(prediction.propertyPath("avg")).`as`("avg")
            ).from(DataSource.collection(testCollection))
            .where(
                prediction.propertyPath("sum").lessThanOrEqualTo(Expression.value(15)).or(
                    prediction.propertyPath("avg").equalTo(Expression.value(8))
                )
            )

        val explain = q.explain()
        assertNotEquals(-1, explain.indexOf("USING INDEX SumIndex"))
        assertNotEquals(-1, explain.indexOf("USING INDEX AvgIndex"))

        verifyQuery(q, 2) { _, r ->
            assertTrue(r.getInt(0) == 15 || r.getInt(1) == 8)
        }
        assertEquals(aggregateModel.numberOfCalls, 2)

        aggregateModel.unregisterModel()
    }

    // predictive indexes are not supported in CBL C SDK
    @IgnoreLinuxMingw
    @Test
    fun testIndexCompoundPredictiveValuesUsingPredictiveIndex() {
        createDocument(listOf(1, 2, 3, 4, 5))
        createDocument(listOf(6, 7, 8, 9, 10))

        val aggregateModel = AggregateModel()
        aggregateModel.registerModel()

        val model = AggregateModel.NAME
        val input = Expression.value(mapOf("numbers" to Expression.property("numbers")))
        val prediction = Function.prediction(model, input)

        val index = IndexBuilder.predictiveIndex(model, input, listOf("sum", "avg"))
        testCollection.createIndex("SumAvgIndex", index)

        val q = QueryBuilder
            .select(
                SelectResult.expression(prediction.propertyPath("sum")).`as`("sum"),
                SelectResult.expression(prediction.propertyPath("avg")).`as`("avg")
            ).from(DataSource.collection(testCollection))
            .where(
                prediction.propertyPath("sum").equalTo(Expression.value(15)).and(
                    prediction.propertyPath("avg").equalTo(Expression.value(3))
                )
            )

        val explain = q.explain()
        assertNotEquals(-1, explain.indexOf("USING INDEX SumAvgIndex"))

        verifyQuery(q, 1) { _, r ->
            assertEquals(r.getInt(0), 15)
            assertEquals(r.getInt(1), 3)
        }
        assertEquals(aggregateModel.numberOfCalls, 2)

        aggregateModel.unregisterModel()
    }

    // predictive indexes are not supported in CBL C SDK
    @IgnoreLinuxMingw
    @Test
    fun testDeletePredictiveIndex() {
        createDocument(listOf(1, 2, 3, 4, 5))
        createDocument(listOf(6, 7, 8, 9, 10))

        val aggregateModel = AggregateModel()
        aggregateModel.registerModel()

        val model = AggregateModel.NAME
        val input = Expression.value(mapOf("numbers" to Expression.property("numbers")))
        val prediction = Function.prediction(model, input)

        // Index:
        val index = IndexBuilder.predictiveIndex(model, input, listOf("sum"))
        testCollection.createIndex("SumIndex", index)

        // Query with index:
        var q: Query = QueryBuilder
            .select(SelectResult.property("numbers"))
            .from(DataSource.collection(testCollection))
            .where(prediction.propertyPath("sum").equalTo(Expression.value(15)))
        var explain = q.explain()
        assertNotEquals(-1, explain.indexOf("USING INDEX SumIndex"))

        verifyQuery(q, 1) { _, r ->
            @Suppress("UNCHECKED_CAST")
            val numbers = r.getArray(0)!!.toList() as List<Int>
            assertTrue(numbers.isNotEmpty())
        }
        assertEquals(aggregateModel.numberOfCalls, 2)

        // Delete SumIndex:
        testCollection.deleteIndex("SumIndex")

        // Query again:
        aggregateModel.reset()
        q = QueryBuilder
            .select(SelectResult.property("numbers"))
            .from(DataSource.collection(testCollection))
            .where(prediction.propertyPath("sum").equalTo(Expression.value(15)))
        explain = q.explain()
        assertEquals(-1, explain.indexOf("USING INDEX SumIndex"))

        val rows = verifyQueryWithEnumerator(q) { _, r ->
            @Suppress("UNCHECKED_CAST")
            val numbers = r.getArray(0)!!.toList() as List<Int>
            assertTrue(numbers.isNotEmpty())
        }
        assertEquals(1, rows)
        assertEquals(2, aggregateModel.numberOfCalls)

        aggregateModel.unregisterModel()
    }

    // predictive indexes are not supported in CBL C SDK
    @IgnoreLinuxMingw
    @Test
    fun testDeletePredictiveIndexesSharingSameCacheTable() {
        createDocument(listOf(1, 2, 3, 4, 5))
        createDocument(listOf(6, 7, 8, 9, 10))

        val aggregateModel = AggregateModel()
        aggregateModel.registerModel()

        val model = AggregateModel.NAME
        val input = Expression.value(mapOf("numbers" to Expression.property("numbers")))
        val prediction = Function.prediction(model, input)

        // Create agg index:
        val aggIndex = IndexBuilder.predictiveIndex(model, input)
        testCollection.createIndex("AggIndex", aggIndex)

        // Create sum index:
        val sumIndex = IndexBuilder.predictiveIndex(model, input, listOf("sum"))
        testCollection.createIndex("SumIndex", sumIndex)

        // Create avg index:
        val avgIndex = IndexBuilder.predictiveIndex(model, input, listOf("avg"))
        testCollection.createIndex("AvgIndex", avgIndex)

        // Query:
        var q: Query = QueryBuilder
            .select(SelectResult.property("numbers"))
            .from(DataSource.collection(testCollection))
            .where(
                prediction.propertyPath("sum").lessThanOrEqualTo(Expression.value(15)).or(
                    prediction.propertyPath("avg").equalTo(Expression.value(8))
                )
            )
        var explain = q.explain()
        assertNotEquals(-1, explain.indexOf("USING INDEX SumIndex"))
        assertNotEquals(-1, explain.indexOf("USING INDEX AvgIndex"))

        verifyQuery(q, 2) { _, r ->
            @Suppress("UNCHECKED_CAST")
            val numbers = r.getArray(0)!!.toList() as List<Int>
            assertTrue(numbers.isNotEmpty())
        }
        assertEquals(2, aggregateModel.numberOfCalls)

        // Delete SumIndex:
        testCollection.deleteIndex("SumIndex")

        // Note: when having only one index, SQLite optimizer doesn't utilize the index
        //       when using OR expr. Hence, explicitly test each index with two queries:
        aggregateModel.reset()
        q = QueryBuilder
            .select(SelectResult.property("numbers"))
            .from(DataSource.collection(testCollection))
            .where(prediction.propertyPath("sum").equalTo(Expression.value(15)))

        explain = q.explain()
        assertEquals(-1, explain.indexOf("USING INDEX SumIndex"))

        verifyQuery(q, 1) { _, r ->
            @Suppress("UNCHECKED_CAST")
            val numbers = r.getArray(0)!!.toList() as List<Int>
            assertTrue(numbers.isNotEmpty())
        }
        assertEquals(0, aggregateModel.numberOfCalls)

        aggregateModel.reset()
        q = QueryBuilder
            .select(SelectResult.property("numbers"))
            .from(DataSource.collection(testCollection))
            .where(prediction.propertyPath("avg").equalTo(Expression.value(8)))
        explain = q.explain()
        assertNotEquals(-1, explain.indexOf("USING INDEX AvgIndex"))

        verifyQuery(q, 1) { _, r ->
            @Suppress("UNCHECKED_CAST")
            val numbers = r.getArray(0)!!.toList() as List<Int>
            assertTrue(numbers.isNotEmpty())
        }
        assertEquals(0, aggregateModel.numberOfCalls)

        // Delete AvgIndex
        testCollection.deleteIndex("AvgIndex")

        aggregateModel.reset()
        q = QueryBuilder
            .select(SelectResult.property("numbers"))
            .from(DataSource.collection(testCollection))
            .where(prediction.propertyPath("avg").equalTo(Expression.value(8)))
        explain = q.explain()
        assertEquals(-1, explain.indexOf("USING INDEX AvgIndex"))

        verifyQuery(q, 1) { _, r ->
            @Suppress("UNCHECKED_CAST")
            val numbers = r.getArray(0)!!.toList() as List<Int>
            assertTrue(numbers.isNotEmpty())
        }
        assertEquals(0, aggregateModel.numberOfCalls)

        // Delete AggIndex
        testCollection.deleteIndex("AggIndex")

        aggregateModel.reset()
        q = QueryBuilder
            .select(SelectResult.property("numbers"))
            .from(DataSource.collection(testCollection))
            .where(
                prediction.propertyPath("sum").lessThanOrEqualTo(Expression.value(15)).or(
                    prediction.propertyPath("avg").equalTo(Expression.value(8))
                )
            )
        explain = q.explain()
        assertEquals(-1, explain.indexOf("USING INDEX SumIndex"))
        assertEquals(-1, explain.indexOf("USING INDEX AvgIndex"))

        verifyQuery(q, 2) { _, r ->
            @Suppress("UNCHECKED_CAST")
            val numbers = r.getArray(0)!!.toList() as List<Int>
            assertTrue(numbers.isNotEmpty())
        }
        assertTrue(aggregateModel.numberOfCalls > 0)

        aggregateModel.unregisterModel()
    }

    @Test
    fun testEuclideanDistance() {
        val tests = listOf(
            listOf(listOf(10, 10), listOf(13, 14), 5),
            listOf(listOf(1, 2, 3), listOf(1, 2, 3), 0),
            listOf(listOf<Int>(), listOf<Int>(), 0),
            listOf(listOf(1, 2), listOf(1, 2, 3), null),
            listOf(listOf(1, 2), "foo", null)
        )

        for (test in tests) {
            val doc = MutableDocument()
            doc.setValue("v1", test[0])
            doc.setValue("v2", test[1])
            doc.setValue("distance", test[2])
            testCollection.save(doc)
        }

        val distance = Function.euclideanDistance(
            Expression.property("v1"),
            Expression.property("v2")
        )
        val q = QueryBuilder
            .select(SelectResult.expression(distance), SelectResult.property("distance"))
            .from(DataSource.collection(testCollection))

        verifyQuery(q, tests.size) { _, r ->
            if (r.getValue(1) == null) {
                assertNull(r.getValue(0))
            } else {
                assertEquals(r.getInt(0), r.getInt(1))
            }
        }
    }

    @Test
    fun testSquaredEuclideanDistance() {
        val tests = listOf(
            listOf(listOf(10, 10), listOf(13, 14), 25),
            listOf(listOf(1, 2, 3), listOf(1, 2, 3), 0),
            listOf(listOf<Int>(), listOf<Int>(), 0),
            listOf(listOf(1, 2), listOf(1, 2, 3), null),
            listOf(listOf(1, 2), "foo", null)
        )

        for (test in tests) {
            val doc = MutableDocument()
            doc.setValue("v1", test[0])
            doc.setValue("v2", test[1])
            doc.setValue("distance", test[2])
            testCollection.save(doc)
        }

        val distance = Function.squaredEuclideanDistance(
            Expression.property("v1"),
            Expression.property("v2")
        )
        val q = QueryBuilder
            .select(SelectResult.expression(distance), SelectResult.property("distance"))
            .from(DataSource.collection(testCollection))

        verifyQuery(q, tests.size) { _, r ->
            if (r.getValue(1) == null) {
                assertNull(r.getValue(0))
            } else {
                assertEquals(r.getInt(0), r.getInt(1))
            }
        }
    }

    @Test
    fun testCosineDistance() {
        val tests = listOf(
            listOf(listOf(10, 0), listOf(0, 99), 1.0),
            listOf(listOf(1, 2, 3), listOf(1, 2, 3), 0.0),
            listOf(listOf(1, 0, -1), listOf(-1, -1, 0), 1.5),
            listOf(listOf<Int>(), listOf(), null),
            listOf(listOf(1, 2), listOf(1, 2, 3), null),
            listOf(listOf(1, 2), "foo", null)
        )

        for (test in tests) {
            val doc = MutableDocument()
            doc.setValue("v1", test[0])
            doc.setValue("v2", test[1])
            doc.setValue("distance", test[2])
            testCollection.save(doc)
        }

        val distance = Function.cosineDistance(
            Expression.property("v1"),
            Expression.property("v2")
        )
        val q = QueryBuilder
            .select(SelectResult.expression(distance), SelectResult.property("distance"))
            .from(DataSource.collection(testCollection))

        verifyQuery(q, tests.size) { _, r ->
            if (r.getValue(1) == null) {
                assertNull(r.getValue(0))
            } else {
                assertEquals(r.getDouble(0), r.getDouble(1))
            }
        }
    }
}

abstract class TestPredictiveModel : PredictiveModel {

    abstract val name: String

    var numberOfCalls = 0

    override fun predict(input: Dictionary): Dictionary? {
        numberOfCalls++
        return doPredict(input)
    }

    abstract fun doPredict(input: Dictionary): Dictionary?

    fun registerModel() {
        Database.prediction.registerModel(name, this)
    }

    fun unregisterModel() {
        Database.prediction.unregisterModel(name)
    }

    fun reset() {
        numberOfCalls = 0
    }
}

class EchoModel : TestPredictiveModel() {

    override val name = NAME

    override fun doPredict(input: Dictionary): Dictionary {
        return input
    }

    companion object {
        const val NAME = "EchoModel"
    }
}

class AggregateModel : TestPredictiveModel() {

    override val name = NAME

    override fun doPredict(input: Dictionary): Dictionary? {
        @Suppress("UNCHECKED_CAST")
        val numbers = input.getArray("numbers")?.toList() as List<Int>? ?: return null

        val output = MutableDictionary()
        output.setValue("sum", numbers.sum())
        output.setValue("min", numbers.min())
        output.setValue("max", numbers.max())
        output.setValue("avg", numbers.average().toInt())
        return output
    }

    companion object {
        const val NAME = "AggregateModel"
    }
}

class TextModel : TestPredictiveModel() {

    override val name = NAME

    override fun doPredict(input: Dictionary): Dictionary? {
        val blob = input.getBlob("text") ?: return null

        val contentType = blob.contentType
        if (contentType != "text/plain") {
            println("WARN: Invalid blob content type; not text/plain.")
            return null
        }

        val text = blob.content!!.decodeToString()

        val wc = text.split("""\s""".toRegex()).size
        val sc = text.split('.', '?', '!').size - 1

        val output = MutableDictionary()
        output.setInt("wc", wc)
        output.setInt("sc", sc)
        return output
    }

    companion object {
        const val NAME = "TextModel"
    }
}
