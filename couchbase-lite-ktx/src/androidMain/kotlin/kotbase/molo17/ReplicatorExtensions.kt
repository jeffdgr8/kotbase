/*
 * Copyright (c) 2020 MOLO17
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Modified by Jeff Lockhart
 * - Use kotbase package
 * - Resolve explicitApiWarning() requirements
 */

@file:JvmName("ReplicatorExtensionsJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase.molo17

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotbase.Replicator

/**
 * Binds the [Replicator] instance to the given [Lifecycle].
 *
 * The replicator will be automatically started on the ON_RESUME event,
 * and stopped on the ON_PAUSE event.
 *
 * @see Lifecycle
 * @see Lifecycle.Event.ON_RESUME
 * @see Lifecycle.Event.ON_PAUSE
 */
public fun Replicator.bindToLifecycle(lifecycle: Lifecycle) {
    lifecycle.addObserver(ReplicatorLifecycleObserver(this))
}

/**
 * Provides a binding between the Android lifecycle and the Replicator lifecycle.
 *
 * The replicator will be automatically started on the ON_RESUME event,
 * and stopped on the ON_PAUSE event.
 */
internal class ReplicatorLifecycleObserver(
    private val replicator: Replicator
) : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> replicator.start()
            Lifecycle.Event.ON_PAUSE -> replicator.stop()
            Lifecycle.Event.ON_DESTROY -> source.lifecycle.removeObserver(this)
            else -> {
                // ignored.
            }
        }
    }
}