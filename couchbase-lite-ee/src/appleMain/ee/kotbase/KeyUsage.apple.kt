/*
 * Copyright 2026 Jeff Lockhart
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

import cocoapods.CouchbaseLite.CBLKeyUsages
import cocoapods.CouchbaseLite.kCBLKeyUsagesClientAuth
import cocoapods.CouchbaseLite.kCBLKeyUsagesServerAuth

public actual enum class KeyUsage {
    SERVER_AUTH,
    CLIENT_AUTH;

    internal val actual: CBLKeyUsages
        get() = when (this) {
            SERVER_AUTH -> kCBLKeyUsagesServerAuth
            CLIENT_AUTH -> kCBLKeyUsagesClientAuth
        }
}

internal val Set<KeyUsage>.actual: CBLKeyUsages
    get() = fold(0UL) { acc, usage -> acc or usage.actual }
