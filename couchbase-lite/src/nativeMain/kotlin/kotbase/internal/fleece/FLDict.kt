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
