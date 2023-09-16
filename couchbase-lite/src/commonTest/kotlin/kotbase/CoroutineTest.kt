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

import kotlin.test.Test

class CoroutineTest : BaseCoroutineTest() {

    // DatabaseChange

    @Test
    fun testDatabaseChangeOnCoroutineContext() {
        testOnCoroutineContext(
            addListener = { context, work ->
                baseTestDb.addChangeListener(context) {
                    work()
                }
            },
            change = {
                saveDocInBaseTestDb(MutableDocument("newDoc"))
            }
        )
    }

    @Test
    fun testDatabaseChangeCoroutineCanceled() {
        testCoroutineCanceled(
            addListener = { context, work ->
                baseTestDb.addChangeListener(context) {
                    work()
                }
            },
            change = {
                saveDocInBaseTestDb(MutableDocument("newDoc"))
            },
            removeListener = { token ->
                baseTestDb.removeChangeListener(token)
            }
        )
    }

    @Test
    fun testDatabaseChangeCoroutineScopeListenerRemoved() {
        testCoroutineScopeListenerRemoved(
            addListener = { scope, work ->
                baseTestDb.addChangeListener(scope) {
                    work()
                }
            },
            listenedChange = {
                saveDocInBaseTestDb(MutableDocument("withListener"))
            },
            notListenedChange = {
                saveDocInBaseTestDb(MutableDocument("noListener"))
            }
        )
    }

    // DocumentChange

    @Test
    fun testDocumentChangeOnCoroutineContext() {
        val id = "testDoc"
        val doc = MutableDocument(id)
        doc.setString("property", "initial value")
        saveDocInBaseTestDb(doc)

        testOnCoroutineContext(
            addListener = { context, work ->
                baseTestDb.addDocumentChangeListener(id, context) {
                    work()
                }
            },
            change = {
                doc.setString("property", "changed value")
                saveDocInBaseTestDb(doc)
            }
        )
    }

    @Test
    fun testDocumentChangeCoroutineCanceled() {
        val id = "testDoc"
        val doc = MutableDocument(id)
        doc.setString("property", "initial value")
        saveDocInBaseTestDb(doc)

        testCoroutineCanceled(
            addListener = { context, work ->
                baseTestDb.addDocumentChangeListener(id, context) {
                    work()
                }
            },
            change = {
                doc.setString("property", "changed value")
                saveDocInBaseTestDb(doc)
            },
            removeListener = { token ->
                baseTestDb.removeChangeListener(token)
            }
        )
    }

    @Test
    fun testDocumentChangeCoroutineScopeListenerRemoved() {
        val id = "testDoc"
        val doc = MutableDocument(id)
        doc.setString("property", "initial value")
        saveDocInBaseTestDb(doc)

        testCoroutineScopeListenerRemoved(
            addListener = { scope, work ->
                baseTestDb.addDocumentChangeListener(id, scope) {
                    work()
                }
            },
            listenedChange = {
                doc.setString("property", "listened change")
                saveDocInBaseTestDb(doc)
            },
            notListenedChange = {
                doc.setString("property", "not listened change")
                saveDocInBaseTestDb(doc)
            }
        )
    }

    // QueryChange

    @Test
    fun testQueryChangeOnCoroutineContext() {
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(baseTestDb))

        testOnCoroutineContext(
            addListener = { context, work ->
                query.addChangeListener(context) {
                    work()
                }
            },
            change = {
                saveDocInBaseTestDb(MutableDocument("newDoc"))
            }
        )
    }

    @Test
    fun testQueryChangeCoroutineCanceled() {
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(baseTestDb))

        testCoroutineCanceled(
            addListener = { context, work ->
                query.addChangeListener(context) {
                    work()
                }
            },
            change = {
                saveDocInBaseTestDb(MutableDocument("newDoc"))
            },
            removeListener = { token ->
                query.removeChangeListener(token)
            }
        )
    }

    @Test
    fun testQueryChangeCoroutineScopeListenerRemoved() {
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(baseTestDb))

        testCoroutineScopeListenerRemoved(
            addListener = { scope, work ->
                query.addChangeListener(scope) {
                    work()
                }
            },
            listenedChange = {
                saveDocInBaseTestDb(MutableDocument("listenedDoc"))
            },
            notListenedChange = {
                saveDocInBaseTestDb(MutableDocument("notListenedDoc"))
            }
        )
    }
}
