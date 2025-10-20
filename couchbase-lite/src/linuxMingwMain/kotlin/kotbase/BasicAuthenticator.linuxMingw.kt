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
package kotbase

import cnames.structs.CBLAuthenticator
import kotbase.internal.fleece.toFLString
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.memScoped
import libcblite.CBLAuth_CreatePassword

public actual class BasicAuthenticator
private constructor(
    public actual val username: String,
    public actual val passwordChars: CharArray,
    actual: CPointer<CBLAuthenticator>
) : Authenticator(actual) {

    public actual constructor(username: String, password: CharArray) : this(
        username,
        password,
        memScoped {
            debug.CBLAuth_CreatePassword(
                username.toFLString(this),
                password.concatToString().toFLString(this)
            )!!
        }
    )
}
