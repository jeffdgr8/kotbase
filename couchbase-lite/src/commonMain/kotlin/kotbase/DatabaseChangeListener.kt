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

/**
 * The listener interface for receiving Database change events.
 *
 * Callback function from Database when database has changed
 *
 * @param change the database change information
 */
@Deprecated("Use CollectionChangeListener")
public typealias DatabaseChangeListener = ChangeListener<DatabaseChange>

/**
 * The listener interface for receiving Database change events, called within a coroutine.
 *
 * Callback function from Database when database has changed
 *
 * @param change the database change information
 */
@Deprecated("Use CollectionChangeSuspendListener")
public typealias DatabaseChangeSuspendListener = ChangeSuspendListener<DatabaseChange>