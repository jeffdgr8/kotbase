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

import com.couchbase.lite.internal.CouchbaseLiteInternal
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

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
