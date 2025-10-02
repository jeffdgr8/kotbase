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
@file:Suppress("MemberVisibilityCanBePrivate")

package kotbase.internal.utils

import kotlinx.serialization.json.*

object JSONUtils {

    fun fromJSON(json: JsonObject): Map<String, Any?> =
        json.mapValues { fromJSON(it.value) }

    fun fromJSON(json: JsonArray): List<Any?> =
        json.map { fromJSON(it) }

    private fun fromJSON(value: JsonElement): Any? = when (value) {
        is JsonObject -> fromJSON(value)
        is JsonArray -> fromJSON(value)
        is JsonNull -> null
        is JsonPrimitive -> with(value) {
            when {
                isString -> content
                else -> booleanOrNull ?: run {
                    if ('.' in content) {
                        content.toDouble()
                    } else {
                        content.toLong().toIntIfPossible()
                    }
                }
            }
        }
    }

    private fun Long.toIntIfPossible(): Number = if (this >= Int.MIN_VALUE && this <= Int.MAX_VALUE) toInt() else this
}

fun JsonObject(string: String): JsonObject = Json.parseToJsonElement(string).jsonObject

fun JsonArray(string: String): JsonArray = Json.parseToJsonElement(string).jsonArray
