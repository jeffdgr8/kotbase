package kotbase.internal.fleece

import kotbase.internal.DbContext
import kotlinx.cinterop.memScoped
import libcblite.*

internal fun FLDict.toMap(ctxt: DbContext?): Map<String, Any?> {
    return buildMap {
        memScoped {
            this@toMap.iterator(this).forEach {
                put(it.first, it.second.toObject(ctxt))
            }
        }
    }
}

internal fun Map<String, String>.toFLDict(): FLDict {
    return FLMutableDict_New()!!.apply {
        forEach { (key, value) ->
            FLMutableDict_SetString(this, key.toFLString(), value.toFLString())
        }
    }
}

internal fun FLDict.keys(): List<String> {
    return memScoped {
        iterator(this)
            .asSequence()
            .map { it.first }
            .toList()
    }
}

internal fun FLDict.getValue(key: String): FLValue? {
    return memScoped {
        FLDict_Get(this@getValue, key.toFLString(this))
    }
}