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
 * Changes the database's encryption key, or removes encryption if the new key is null.
 *
 * @param encryptionKey The encryption key
 * @throws CouchbaseLiteException on error
 */
@Throws(CouchbaseLiteException::class)
public expect fun Database.changeEncryptionKey(encryptionKey: EncryptionKey?)

/**
 * **ENTERPRISE EDITION API**
 *
 * The predictive model manager for registering and unregistering predictive models.
 * This is part of the Public API.
 */
public expect val Database.Companion.prediction: Prediction
