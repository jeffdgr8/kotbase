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
 * An encryption key for a database. This is a symmetric key that be kept secret.
 * It should be stored either in the Keychain, or in the user's memory (hopefully not a sticky note.)
 */
public expect class EncryptionKey {

    /**
     * Initializes the encryption key with a raw AES-128 key 16 bytes in length.
     * To create a key, generate random data using a secure cryptographic randomizer.
     *
     * @param key The raw AES-128 key data.
     */
    public constructor(key: ByteArray)

    /**
     * Initializes the encryption key from the given password string.
     * The password string will be internally converted to a raw AES-128 key using 64,000 rounds of PBKDF2 hashing.
     *
     * @param password The password string.
     */
    public constructor(password: String)
}
