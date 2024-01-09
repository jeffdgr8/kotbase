/*
 * Copyright 2024 Jeff Lockhart
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
package kotbase.ktx

import kotbase.MutableDocument

/**
 * Creates a new Document with a given ID and content from the passed key/value pairs.
 * If the id is null, the document will be created with a new random UUID.
 * Allowed value types are List, Instant, Map, Number, null, String, Array, Blob, and Dictionary.
 * The List and Map must contain only the above types.
 * The created document will be saved into a database when you call
 * the Database's save(Document) method with the document object given.
 *
 * @param id    the document ID
 * @param pairs the content key/value pairs
 */
public fun mutableDocOf(id: String?, vararg pairs: Pair<String, Any?>): MutableDocument =
    MutableDocument(id, pairs.toMap())

/**
 * Creates a new Document with a new random UUID and the key/value pairs as the content.
 * Allowed value types are List, Instant, Map, Number, null, String, Array, Blob, and Dictionary.
 * If present, Lists, Arrays, Maps and Dictionaries may contain only the above types.
 * The created document will be saved into a database when you call Database.save(Document)
 * with this document object.
 *
 * @param pairs the content key/value pairs
 */
public fun mutableDocOf(vararg pairs: Pair<String, Any?>): MutableDocument =
    MutableDocument(pairs.toMap())
