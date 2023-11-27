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
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import libcblite.*

internal fun FLArray.toList(ctxt: DbContext?): List<Any?> {
    return buildList {
        memScoped {
            this@toList.iterator(this).forEach {
                add(it.toObject(ctxt))
            }
        }
    }
}

internal fun List<String>.toFLArray(): FLArray {
    return FLMutableArray_New()!!.apply {
        forEach {
            memScoped {
                FLMutableArray_AppendString(this@apply, it.toFLString(this))
            }
        }
    }
}

internal fun FLArray.getValue(index: Int): FLValue? =
    FLArray_Get(this, index.convert())
