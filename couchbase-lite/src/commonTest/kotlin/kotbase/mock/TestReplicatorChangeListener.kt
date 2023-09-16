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
package kotbase.mock

import kotbase.ReplicatorActivityLevel
import kotbase.ReplicatorChange
import kotbase.ReplicatorChangeListener
import kotbase.internal.utils.Report
import kotbase.test.lockWithTimeout
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import kotlin.time.Duration

class TestReplicatorChangeListener : ReplicatorChangeListener {

    private val mutex = Mutex(true)

    var error: Throwable? by atomic(null)

    suspend fun awaitCompletion(timeout: Duration): Boolean =
        mutex.lockWithTimeout(timeout)

    override fun invoke(change: ReplicatorChange) {
        val status = change.status
        val error = status.error
        val state = status.activityLevel
        Report.log("Test replicator state change: $state", error)

        this.error = error

        if (change.replicator.config.isContinuous
            && status.activityLevel == ReplicatorActivityLevel.IDLE
            && status.progress.completed == status.progress.total
        ) {
            change.replicator.stop()
        }

        if (status.activityLevel == ReplicatorActivityLevel.STOPPED) {
            mutex.unlock()
        }
    }
}
