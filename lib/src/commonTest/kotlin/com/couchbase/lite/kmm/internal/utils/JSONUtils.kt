package com.couchbase.lite.kmm.internal.utils

import kotlinx.serialization.json.*

object JSONUtils {

    fun fromJSON(json: JsonObject): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        val itr = json.iterator()
        while (itr.hasNext()) {
            val entry = itr.next()
            result[entry.key] = fromJSON(entry.value)
        }
        return result
    }

    fun fromJSON(json: JsonArray): List<Any?> {
        val result = mutableListOf<Any?>()
        for (i in 0 until json.size) {
            result.add(fromJSON(json[i]))
        }
        return result
    }

    private fun fromJSON(value: JsonElement): Any? {
        return when (value) {
            is JsonObject -> fromJSON(value)
            is JsonArray -> fromJSON(value)
            is JsonNull -> null
            is JsonPrimitive -> with(value) {
                when {
                    isString -> content
                    else -> booleanOrNull ?: {
                        if ('.' in content) {
                            content.toDouble()
                        } else {
                            content.toLong().toIntIfPossible()
                        }
                    }
                }
            }
        }
    }

    private fun Long.toIntIfPossible(): Number {
        return if (this > Int.MIN_VALUE && this < Int.MAX_VALUE) {
            toInt()
        } else {
            this
        }
    }
}
