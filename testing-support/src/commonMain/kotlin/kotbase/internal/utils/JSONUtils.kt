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