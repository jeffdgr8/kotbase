/*
 * Copyright 2023 Jeff Lockhart
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

import kotbase.util.to
import kotlinx.cinterop.staticCFunction
import libcblite.CBLConflictHandler

internal class ConflictHandlerWrapper(
    val db: Database,
    val handler: ConflictHandler,
    var exception: Exception? = null
)

internal fun nativeConflictHandler(): CBLConflictHandler {
    return staticCFunction { ref, document, oldDocument ->
        with(ref.to<ConflictHandlerWrapper>()) {
            try {
                handler(
                    MutableDocument(document!!, db),
                    oldDocument?.asDocument(db)
                )
            } catch (e: Exception) {
                // save a reference, as Linux will propagate
                // the error as an "unknown C++ exception"
                exception = e
                throw e
            }
        }
    }
}
