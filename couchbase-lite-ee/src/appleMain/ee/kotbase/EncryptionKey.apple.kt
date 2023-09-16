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

import cocoapods.CouchbaseLite.CBLEncryptionKey
import kotbase.base.DelegatedClass
import kotbase.ext.toNSData

public actual class EncryptionKey
internal constructor(actual: CBLEncryptionKey) : DelegatedClass<CBLEncryptionKey>(actual) {

    public actual constructor(key: ByteArray) : this(CBLEncryptionKey(key.toNSData()))

    public actual constructor(password: String) : this(CBLEncryptionKey(password))
}

internal fun CBLEncryptionKey.asEncryptionKey(): EncryptionKey =
    EncryptionKey(this)
