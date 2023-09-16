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

import kotbase.internal.toException
import kotlinx.cinterop.*
import libcblite.CBLReplicatorStatus

public actual class ReplicatorStatus {

    internal constructor(actual: CPointer<CBLReplicatorStatus>) {
        activityLevel = ReplicatorActivityLevel.from(actual.pointed.activity)
        progress = ReplicatorProgress(actual.pointed.progress)
        error = actual.pointed.error.toException()
    }

    internal constructor(actual: CValue<CBLReplicatorStatus>) {
        lateinit var tempActivity: ReplicatorActivityLevel
        lateinit var tempProgress: ReplicatorProgress
        var tempError: CouchbaseLiteException? = null
        actual.useContents {
            tempActivity = ReplicatorActivityLevel.from(activity)
            tempProgress = ReplicatorProgress(progress)
            tempError = error.toException()
        }
        activityLevel = tempActivity
        progress = tempProgress
        error = tempError
    }

    public actual val activityLevel: ReplicatorActivityLevel

    public actual val progress: ReplicatorProgress

    public actual val error: CouchbaseLiteException?

    override fun toString(): String = "Status{activityLevel=$activityLevel, progress=$progress, error=$error}"
}
