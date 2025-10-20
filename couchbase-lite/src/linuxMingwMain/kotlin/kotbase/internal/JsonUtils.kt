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
package kotbase.internal

import kotbase.MutableArray
import kotbase.MutableDictionary
import kotbase.internal.fleece.toFLString
import kotbase.internal.fleece.toKString
import kotbase.internal.fleece.toObject
import kotbase.internal.fleece.wrapFLError
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret
import libcblite.FLDoc_FromJSON
import libcblite.FLDoc_GetRoot
import libcblite.FLDoc_Release
import libcblite.FLValue_ToJSON

internal object JsonUtils {

    fun parseJson(json: String): Any? {
        val doc = wrapFLError { error ->
            memScoped {
                debug.FLDoc_FromJSON(json.toFLString(this), error)
            }
        }
        return FLDoc_GetRoot(doc)?.toObject(null, false).also {
            debug.FLDoc_Release(doc)
        }
    }

    fun toJson(map: Map<String, Any?>): String =
        debug.FLValue_ToJSON(MutableDictionary(map).actual.reinterpret()).toKString()!!

    fun toJson(list: List<Any?>): String =
        debug.FLValue_ToJSON(MutableArray(list).actual.reinterpret()).toKString()!!
}
