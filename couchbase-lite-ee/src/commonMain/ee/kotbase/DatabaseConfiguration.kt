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
 * **ENTERPRISE EDITION API**
 *
 * Set a key to encrypt the database with. If the database does not exist and is being created,
 * it will use this key, and the same key must be given every time it's opened
 *
 * @param encryptionKey the key
 * @return this.
 */
public fun DatabaseConfiguration.setEncryptionKey(encryptionKey: EncryptionKey?): DatabaseConfiguration {
    this.encryptionKey = encryptionKey
    return this
}

/**
 * **ENTERPRISE EDITION API**
 *
 * A key to encrypt the database with.
 */
public expect var DatabaseConfiguration.encryptionKey: EncryptionKey?
