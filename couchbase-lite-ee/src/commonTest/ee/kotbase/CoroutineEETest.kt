package kotbase

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class CoroutineEETest : BaseCoroutineTest() {

    private fun runWithReplicator(test: (Replicator) -> Unit) {
        val target = DatabaseEndpoint(otherDB)
        val config = makeConfig(target, ReplicatorType.PUSH, false)
        val replicator = testReplicator(config)
        val stopped = Mutex(true)
        replicator.addChangeListener {
            if (it.status.activityLevel == ReplicatorActivityLevel.STOPPED) {
                stopped.unlock()
            }
        }
        test(replicator)
        replicator.stop()
        runBlocking {
            withTimeout(STD_TIMEOUT_SEC.seconds) {
                stopped.lock()
            }
        }
    }

    // ReplicatorChange

    @Test
    fun testReplicatorChangeOnCoroutineContext() = runWithReplicator { replicator ->
        testOnCoroutineContext(
            addListener = { context, work ->
                replicator.addChangeListener(context) {
                    work()
                }
                replicator.start()
            },
            change = {
                saveDocInBaseTestDb(MutableDocument("newDoc"))
            }
        )
    }

    @Test
    fun testReplicatorChangeCoroutineCanceled() = runWithReplicator { replicator ->
        testCoroutineCanceled(
            addListener = { context, work ->
                replicator.addChangeListener(context) {
                    work()
                }.also {
                    replicator.start()
                }
            },
            change = {
                saveDocInBaseTestDb(MutableDocument("newDoc"))
            },
            removeListener = { token ->
                replicator.removeChangeListener(token)
            }
        )
    }

    @Test
    fun testReplicatorChangeCoroutineScopeListenerRemoved() = runWithReplicator { replicator ->
        testCoroutineScopeListenerRemoved(
            addListener = { scope, work ->
                replicator.addChangeListener(scope) {
                    work()
                }
                replicator.start()
            },
            listenedChange = {
                saveDocInBaseTestDb(MutableDocument("listenedDoc"))
            },
            notListenedChange = {
                saveDocInBaseTestDb(MutableDocument("notListenedDoc"))
            }
        )
    }

    // DocumentReplication

    @Test
    fun testDocumentReplicationOnCoroutineContext() = runWithReplicator { replicator ->
        testOnCoroutineContext(
            addListener = { context, work ->
                replicator.addDocumentReplicationListener(context) {
                    work()
                }
                replicator.start()
            },
            change = {
                saveDocInBaseTestDb(MutableDocument("newDoc"))
            }
        )
    }

    @Test
    fun testDocumentReplicationCoroutineCanceled() = runWithReplicator { replicator ->
        testCoroutineCanceled(
            addListener = { context, work ->
                replicator.addDocumentReplicationListener(context) {
                    work()
                }.also {
                    replicator.start()
                }
            },
            change = {
                saveDocInBaseTestDb(MutableDocument("newDoc"))
            },
            removeListener = { token ->
                replicator.removeChangeListener(token)
            }
        )
    }

    @Test
    fun testDocumentReplicationCoroutineScopeListenerRemoved() = runWithReplicator { replicator ->
        testCoroutineScopeListenerRemoved(
            addListener = { scope, work ->
                replicator.addDocumentReplicationListener(scope) {
                    work()
                }
                replicator.start()
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