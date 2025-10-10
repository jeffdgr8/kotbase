/*
 * Copyright 2025 Jeff Lockhart
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

import kotbase.internal.fleece.toFLString
import kotbase.internal.wrapCBLError
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import libcblite.CBL_EnableVectorSearch
import platform.posix.getenv

public actual object Extension {

    public actual fun enableVectorSearch() {
        wrapCBLError { error ->
            memScoped {
                val libPath = getenv("CBLITE_VECTOR_SEARCH_LIB_PATH")?.toKString() ?: "."
                CBL_EnableVectorSearch(libPath.toFLString(this), error)
            }
        }
    }
}
