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
package kotbase

import kotbase.ext.toKotlinInstant
import kotbase.internal.DelegatedClass
import kotlinx.datetime.Instant
import kotlin.reflect.safeCast
import com.couchbase.lite.Array as CBLArray
import com.couchbase.lite.Dictionary as CBLDictionary
import com.couchbase.lite.IndexUpdater as CBLIndexUpdater

internal class IndexUpdaterImpl(actual: CBLIndexUpdater) : DelegatedClass<CBLIndexUpdater>(actual), IndexUpdater {

    private val collectionMap: MutableMap<Int, Any> = mutableMapOf()

    override val count: Int
        get() = actual.count()

    override fun getValue(index: Int): Any? {
        return collectionMap[index]
            ?: actual.getValue(index)?.delegateIfNecessary()
                ?.also { if (it is Array || it is Dictionary) collectionMap[index] = it }
    }

    override fun getString(index: Int): String? =
        actual.getString(index)

    override fun getNumber(index: Int): Number? =
        actual.getNumber(index)

    override fun getInt(index: Int): Int =
        actual.getInt(index)

    override fun getLong(index: Int): Long =
        actual.getLong(index)

    override fun getFloat(index: Int): Float =
        actual.getFloat(index)

    override fun getDouble(index: Int): Double =
        actual.getDouble(index)

    override fun getBoolean(index: Int): Boolean =
        actual.getBoolean(index)

    override fun getBlob(index: Int): Blob? =
        actual.getBlob(index)?.asBlob()

    override fun getDate(index: Int): Instant? =
        actual.getDate(index)?.toKotlinInstant()

    override fun getArray(index: Int): Array? {
        return getInternalCollection(index)
            ?: (actual.getArray(index) as CBLArray?)?.asArray()
                ?.also { collectionMap[index] = it }
    }

    override fun getDictionary(index: Int): Dictionary? {
        return getInternalCollection(index)
            ?: (actual.getDictionary(index) as CBLDictionary?)?.asDictionary()
                ?.also { collectionMap[index] = it }
    }

    override fun toList(): List<Any?> =
        actual.toList().delegateIfNecessary()

    override fun toJSON(): String =
        actual.toJSON()

    override fun iterator(): Iterator<Any?> = object : Iterator<Any?> {

        private val itr = actual.iterator()

        override fun hasNext(): Boolean = itr.hasNext()

        override fun next(): Any? = itr.next()?.delegateIfNecessary()
    }

    override fun setVector(value: List<Float>?, index: Int) {
        actual.setVector(value, index)
    }

    override fun skipVector(index: Int) {
        actual.skipVector(index)
    }

    override fun finish() {
        actual.finish()
    }

    override fun close() {
        actual.close()
    }

    private inline fun <reified T : Any> getInternalCollection(index: Int): T? =
        T::class.safeCast(collectionMap[index])
}

internal fun CBLIndexUpdater.asIndexUpdater(): IndexUpdater = IndexUpdaterImpl(this)
