/*
 * Copyright 2023 Jeff Lockhart
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
package com.couchbase.lite

import kotbase.Collection
import kotbase.internal.DelegatedClass
import com.couchbase.lite.internal.core.C4Document as CBLC4Document

internal actual fun Collection.getC4Document(id: String): C4Document =
    C4Document(actual.getC4Document(id))

internal actual class C4Document(actual: CBLC4Document) : DelegatedClass<CBLC4Document>(actual) {

    actual fun isRevDeleted(): Boolean =
        actual.isRevDeleted
}
