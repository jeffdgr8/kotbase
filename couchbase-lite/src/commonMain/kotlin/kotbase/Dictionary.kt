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

import kotlin.time.Instant
import kotlin.reflect.safeCast

/**
 * Dictionary provides readonly access to dictionary data.
 */
public expect open class Dictionary : DictionaryInterface, Iterable<String> {

    internal val collectionMap: MutableMap<String, Any>

    /**
     * Return a mutable copy of the dictionary
     *
     * @return the MutableDictionary instance
     */
    public fun toMutable(): MutableDictionary

    override val count: Int

    override val keys: List<String>

    override operator fun contains(key: String): Boolean

    override fun getValue(key: String): Any?

    override fun getString(key: String): String?

    override fun getNumber(key: String): Number?

    override fun getInt(key: String): Int

    override fun getLong(key: String): Long

    override fun getFloat(key: String): Float

    override fun getDouble(key: String): Double

    override fun getBoolean(key: String): Boolean

    override fun getBlob(key: String): Blob?

    override fun getDate(key: String): Instant?

    override fun getArray(key: String): Array?

    override fun getDictionary(key: String): Dictionary?

    override fun toMap(): Map<String, Any?>

    override fun toJSON(): String

    /**
     * An iterator over keys of this Dictionary.
     * A call to the `next()` method of the returned iterator
     * will throw a ConcurrentModificationException, if the MutableDictionary is
     * modified while it is in use.
     *
     * @return an iterator over the dictionary's keys.
     */
    override fun iterator(): Iterator<String>
}

internal inline fun <reified T : Any> Dictionary.getInternalCollection(key: String): T? =
    T::class.safeCast(collectionMap[key])
