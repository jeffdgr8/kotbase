package kotbase

import kotbase.internal.JsonUtils
import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
import libcblite.CBLValueIndexConfiguration
import libcblite.kCBLJSONLanguage
import platform.posix.strdup
import platform.posix.strlen

public actual class ValueIndex
internal constructor(private val items: List<ValueIndexItem>) : Index() {

    private fun getJson(): String {
        val data = buildList {
            items.forEach {
                add(it.expression.asJSON())
            }
        }
        return JsonUtils.toJson(data)
    }

    internal fun getActual(): CValue<CBLValueIndexConfiguration> {
        val json = getJson()
        return cValue {
            expressionLanguage = kCBLJSONLanguage
            expressions.buf = strdup(json)
            expressions.size = strlen(json)
        }
    }
}