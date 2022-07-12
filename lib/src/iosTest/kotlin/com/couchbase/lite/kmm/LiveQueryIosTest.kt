//package com.couchbase.lite.kmm
//
//import com.udobny.kmm.test.ThreadUtils
//import kotlinx.cinterop.convert
//import platform.Foundation.NSThread
//import platform.Foundation.NSTimeInterval
//import platform.darwin.dispatch_async
//import platform.darwin.dispatch_get_global_queue
//import platform.posix.QOS_CLASS_DEFAULT
//import kotlin.test.Test
//import kotlin.test.assertEquals
//import kotlin.test.assertNotNull
//
//class LiveQueryIosTest {
//
//    private val KEY = "number"
//    private val db = Database("test")
//
//    @Test
//    fun liveQueryTest() {
//        println("Live query test started")
//        ThreadUtils.logThread()
//
//        Database.log.console.level = LogLevel.DEBUG
//        Database.log.console.domains = LogDomain.ALL_DOMAINS
//
//        val query = QueryBuilder
//            .select(SelectResult.expression(Meta.id))
//            .from(DataSource.database(db))
//
//        var atmCount = 0
//
//        val token = query.addChangeListener { change ->
//            println("Change received!")
//            ThreadUtils.logThread()
//
//            val num = atmCount++
//            val count = change.results!!.allResults().size
//            println("Change #$num with $count results")
//            println("Database count = ${db.count}")
//        }
//
//        sleep(0.5)
//
//        createDocNumbered(10)
//
//        sleep(0.5)
//
//        createDocNumbered(11)
//
//        sleep(10.0)
//
//        println("Removing change listener")
//        query.removeChangeListener(token)
//
//        println("Deleting db")
//        db.delete()
//    }
//
//    private fun createDocNumbered(i: Int) {
//        val docID = "doc-$i"
//        val doc = MutableDocument(docID)
//        doc.setValue(KEY, i)
//        saveDocInDb(doc)
//    }
//
//    private fun saveDocInDb(doc: MutableDocument): Document {
//        db.save(doc)
//        val savedDoc = db.getDocument(doc.id)
//        assertNotNull(savedDoc)
//        assertEquals(doc.id, savedDoc.id)
//        return savedDoc
//    }
//}
//
//class IosAsyncTest {
//
//    @Test
//    fun testAsync() {
//        println("Start")
//        logThread()
//
//        dispatchAsync {
//            println("Async task #1")
//            logThread()
//            sleep(1.0)
//        }
//
//        dispatchAsync {
//            println("Async task #2")
//            logThread()
//            sleep(1.0)
//        }
//
//        dispatchAsync {
//            println("Async task #3")
//            logThread()
//            sleep(1.0)
//        }
//
//        dispatchAsync {
//            println("Async task #4")
//            logThread()
//            sleep(1.0)
//        }
//
//        dispatchAsync {
//            println("Async task #5")
//            logThread()
//            sleep(1.0)
//        }
//
//        sleep(10.0)
//    }
//}
//
//private fun logThread() {
//    println("Thread = ${NSThread.currentThread}")
//}
//
//private fun sleep(interval: NSTimeInterval) {
//    NSThread.sleepForTimeInterval(interval)
//}
//
//private fun dispatchAsync(task: () -> Unit) {
//    dispatch_async(dispatch_get_global_queue(QOS_CLASS_DEFAULT.convert(), 0), task)
//}
