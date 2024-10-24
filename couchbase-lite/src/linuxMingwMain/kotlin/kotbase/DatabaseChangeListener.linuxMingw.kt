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

import kotlinx.coroutines.CoroutineScope

internal sealed class DatabaseChangeListenerHolder(
    val database: Database
)

@Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")
internal class DatabaseChangeDefaultListenerHolder(
    val listener: DatabaseChangeListener,
    database: Database
) : DatabaseChangeListenerHolder(database)

@Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")
internal class DatabaseChangeSuspendListenerHolder(
    val listener: DatabaseChangeSuspendListener,
    database: Database,
    val scope: CoroutineScope
) : DatabaseChangeListenerHolder(database)
