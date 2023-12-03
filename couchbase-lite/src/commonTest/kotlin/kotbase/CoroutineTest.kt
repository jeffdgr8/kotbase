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

    // CollectionChange

    @Test
    fun testDatabaseChangeOnCoroutineContext() {
        testOnCoroutineContext(
            addListener = { context, work ->
                testCollection.addChangeListener(context) {
                    work()
                }
            },
            change = {
                saveDocInCollection(MutableDocument("newDoc"))
            }
        )
    }

    @Test
    fun testDatabaseChangeCoroutineCanceled() {
        testCoroutineCanceled(
            addListener = { context, work ->
                testCollection.addChangeListener(context) {
                    work()
                }
            },
            change = {
                saveDocInCollection(MutableDocument("newDoc"))
            },
            removeListener = { token ->
                token.remove()
            }
        )
    }

    @Test
    fun testDatabaseChangeCoroutineScopeListenerRemoved() {
        testCoroutineScopeListenerRemoved(
            addListener = { scope, work ->
                testCollection.addChangeListener(scope) {
                    work()
                }
            },
            listenedChange = {
                saveDocInCollection(MutableDocument("withListener"))
            },
            notListenedChange = {
                saveDocInCollection(MutableDocument("noListener"))
            }
        )
    }

    // DocumentChange

    @Test
    fun testDocumentChangeOnCoroutineContext() {
        val id = "testDoc"
        val doc = MutableDocument(id)
        doc.setString("property", "initial value")
        saveDocInCollection(doc)

        testOnCoroutineContext(
            addListener = { context, work ->
                testCollection.addDocumentChangeListener(id, context) {
                    work()
                }
            },
            change = {
                doc.setString("property", "changed value")
                saveDocInCollection(doc)
            }
        )
    }

    @Test
    fun testDocumentChangeCoroutineCanceled() {
        val id = "testDoc"
        val doc = MutableDocument(id)
        doc.setString("property", "initial value")
        saveDocInCollection(doc)

        testCoroutineCanceled(
            addListener = { context, work ->
                testCollection.addDocumentChangeListener(id, context) {
                    work()
                }
            },
            change = {
                doc.setString("property", "changed value")
                saveDocInCollection(doc)
            },
            removeListener = { token ->
                token.remove()
            }
        )
    }

    @Test
    fun testDocumentChangeCoroutineScopeListenerRemoved() {
        val id = "testDoc"
        val doc = MutableDocument(id)
        doc.setString("property", "initial value")
        saveDocInCollection(doc)

        testCoroutineScopeListenerRemoved(
            addListener = { scope, work ->
                testCollection.addDocumentChangeListener(id, scope) {
                    work()
                }
            },
            listenedChange = {
                doc.setString("property", "listened change")
                saveDocInCollection(doc)
            },
            notListenedChange = {
                doc.setString("property", "not listened change")
                saveDocInCollection(doc)
            }
        )
    }

    // QueryChange

    @Test
    fun testQueryChangeOnCoroutineContext() {
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(testCollection))

        testOnCoroutineContext(
            addListener = { context, work ->
                query.addChangeListener(context) {
                    work()
                }
            },
            change = {
                saveDocInCollection(MutableDocument("newDoc"))
            }
        )
    }

    @Test
    fun testQueryChangeCoroutineCanceled() {
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(testCollection))

        testCoroutineCanceled(
            addListener = { context, work ->
                query.addChangeListener(context) {
                    work()
                }
            },
            change = {
                saveDocInCollection(MutableDocument("newDoc"))
            },
            removeListener = { token ->
                query.removeChangeListener(token)
            }
        )
    }

    @Test
    fun testQueryChangeCoroutineScopeListenerRemoved() {
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(testCollection))

        testCoroutineScopeListenerRemoved(
            addListener = { scope, work ->
                query.addChangeListener(scope) {
                    work()
                }
            },
            listenedChange = {
                saveDocInCollection(MutableDocument("listenedDoc"))
            },
            notListenedChange = {
                saveDocInCollection(MutableDocument("notListenedDoc"))
            }
        )
    }
}
