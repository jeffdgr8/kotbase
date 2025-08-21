/*
 * Copyright 2025 Jeff Lockhart
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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Instant

class ParameterTest : BaseDbTest() {

    @Test
    fun testCreateParams() {
        val params = Parameters()
        params.setString("param", "value")

        val query = testDatabase.createQuery(
            "SELECT  meta().id"
                    + " FROM _default._default"
                    + " WHERE test = \$param"
        )

        query.parameters = params

        assertEquals("value", query.parameters?.getValue("param"))
    }

    @Test
    fun testMutateImmutableParams() {
        val params = Parameters()
        params.setString("param", "value")

        val query = testDatabase.createQuery(
            "SELECT  meta().id"
                    + " FROM _default._default"
                    + " WHERE test = \$param"
        )

        query.parameters = params
        assertFailsWith<CouchbaseLiteError> {
            query.parameters?.setString("param", "value2")
        }
    }

    @Test
    fun testParamContents() {
        val params = makeParams()

        val query = testDatabase.createQuery(
            "SELECT  meta().id"
                    + " FROM _default._default"
                    + " WHERE test = \$param"
        )

        query.parameters = params

        verifyParams(query.parameters)
    }

    private fun makeParams(): Parameters {
        // A small array
        val simpleArray = MutableArray()
        simpleArray.addInt(54)
        simpleArray.addString("Joplin")

        // A small dictionary
        val simpleDict = MutableDictionary()
        simpleDict.setInt("sparam.1", 58)
        simpleDict.setString("sparam.2", "Winehouse")

        // Parameters:
        val params = Parameters()
        params.setValue("param-1", null)
        params.setBoolean("param-2", true)
        params.setBoolean("param-3", false)
        params.setInt("param-4", 0)
        params.setInt("param-5", Int.MIN_VALUE)
        params.setInt("param-6", Int.MAX_VALUE)
        params.setLong("param-7", 0L)
        params.setLong("param-8", Long.MIN_VALUE)
        params.setLong("param-9", Long.MAX_VALUE)
        params.setFloat("param-10", 0.0f)
        params.setFloat("param-11", Float.MIN_VALUE)
        params.setFloat("param-12", Float.MAX_VALUE)
        params.setDouble("param-13", 0.0)
        params.setDouble("param-14", Double.MIN_VALUE)
        params.setDouble("param-15", Double.MAX_VALUE)
        params.setNumber("param-16", null)
        params.setNumber("param-17", 0)
        params.setNumber("param-18", Float.MIN_VALUE)
        params.setNumber("param-19", Long.MIN_VALUE)
        params.setString("param-20", null)
        params.setString("param-21", "Quatro")
        params.setDate("param-22", null)
        params.setDate("param-23", Instant.parse(TEST_DATE))
        params.setArray("param-24", null)
        params.setArray("param-25", simpleArray)
        params.setDictionary("param-26", null)
        params.setDictionary("param-27", simpleDict)
        return params
    }

    private fun verifyParams(params: Parameters?) {
        assertNotNull(params)

        //#0 param.setValue(null);
        assertNull(params.getValue("param-1"))

        //#1 param.setBoolean(true);
        assertEquals(true, params.getValue("param-2"))

        //#2 param.setBoolean(false);
        assertEquals(false, params.getValue("param-3"))

        //#3 param.setInt(0);
        when (val param4 = params.getValue("param-4")) {
            // JVM is Int
            is Int -> assertEquals(0, param4)
            // iOS is Long
            is Long -> assertEquals(0L, param4)
            null -> fail("param-4 is null")
            else -> fail("param-4 is $param4 (${param4::class})")
        }

        //#4 param.setInt(Integer.MIN_VALUE);
        when (val param5 = params.getValue("param-5")) {
            // JVM is Int
            is Int -> assertEquals(Int.MIN_VALUE, param5)
            // iOS is Long
            is Long -> assertEquals(Int.MIN_VALUE.toLong(), param5)
            null -> fail("param-5 is null")
            else -> fail("param-5 is $param5 (${param5::class})")
        }

        //#5 param.setInt(Integer.MAX_VALUE);
        when (val param6 = params.getValue("param-6")) {
            // JVM is Int
            is Int -> assertEquals(Int.MAX_VALUE, param6)
            // iOS is Long
            is Long -> assertEquals(Int.MAX_VALUE.toLong(), param6)
            null -> fail("param-6 is null")
            else -> fail("param-6 is $param6 (${param6::class})")
        }

        //#6 param.setLong(0L);
        assertEquals(0L, params.getValue("param-7"))

        //#7 param.setLong(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, params.getValue("param-8"))

        //#8 param.setLong(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, params.getValue("param-9"))

        //#9 param.setFloat(0.0F);
        assertEquals(0.0f, params.getValue("param-10"))

        //#10 param.setFloat(Float.MIN_VALUE);
        assertEquals(Float.MIN_VALUE, params.getValue("param-11"))

        //#11 param.setFloat(Float.MAX_VALUE);
        assertEquals(Float.MAX_VALUE, params.getValue("param-12"))

        //#12 param.setDouble(0.0);
        assertEquals(0.0, params.getValue("param-13") as Double, 0.001)

        //#13 param.setDouble(Double.MIN_VALUE);
        assertEquals(Double.MIN_VALUE, params.getValue("param-14") as Double, 0.001)

        //#14 param.setDouble(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, params.getValue("param-15") as Double, 0.001)

        //#15 param.setNumber(null);
        assertNull(params.getValue("param-16"))

        //#16 param.setNumber(0);
        when (val param17 = params.getValue("param-17")) {
            // JVM and iOS are Int
            is Int -> assertEquals(0, param17)
            // C is Long
            is Long -> assertEquals(0L, param17)
            null -> fail("param-17 is null")
            else -> fail("param-17 is $param17 (${param17::class})")
        }

        //#17 param.setNumber(Float.MIN_VALUE);
        assertEquals(Float.MIN_VALUE, params.getValue("param-18"))

        //#18 param.setNumber(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, params.getValue("param-19"))

        //#19 param.setString(null);
        assertNull(params.getValue("param-20"))

        //#20 param.setString("Quatro");
        assertEquals("Quatro", params.getValue("param-21"))

        //#21 param.setDate(null);
        assertNull(params.getValue("param-22"))

        //#22 param.setDate(JSONUtils.toDate(TEST_DATE));
        assertEquals(TEST_DATE, params.getValue("param-23"))

        //#23 param.setArray(null);
        assertNull(params.getValue("param-24"))

        //#24 param.setArray(simpleArray);
        val a = params.getValue("param-25")
        assertTrue(a is Array)
        assertEquals(54, a.getInt(0))
        assertEquals("Joplin", a.getString(1))

        //#25 param.setDictionary(null);
        assertNull(params.getValue("param-26"))

        //#26 param.setDictionary(simpleDict);
        val d = params.getValue("param-27")
        assertTrue(d is Dictionary)
        assertEquals(58, d.getInt("sparam.1"))
        assertEquals("Winehouse", d.getString("sparam.2"))
    }
}
