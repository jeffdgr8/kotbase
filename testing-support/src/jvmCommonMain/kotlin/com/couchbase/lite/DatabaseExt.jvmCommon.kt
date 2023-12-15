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
@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@file:JvmName("DatabaseExtTestingSupport") // Disambiguate from :couchbase-lite module file

package com.couchbase.lite

import kotbase.Database

actual val Database.isOpen: Boolean
    get() = actual.isOpen

actual fun <R> Database.withDbLock(action: () -> R): R =
    synchronized(actual.dbLock, action)
