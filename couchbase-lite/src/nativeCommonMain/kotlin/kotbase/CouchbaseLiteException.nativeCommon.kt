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

public actual class CouchbaseLiteException
internal constructor(
    message: String?,
    cause: Exception?,
    domain: String?,
    code: Int,
    private val info: Map<String, Any?>?
) : Exception(message, cause) {

    public actual constructor(message: String) : this(message, null, null, 0, null)

    public actual constructor(message: String, cause: Exception) :
            this(message, cause, null, 0, null)

    public actual constructor(message: String, domain: String, code: Int) :
            this(message, null, domain, code, null)

    public actual constructor(message: String, cause: Exception, domain: String, code: Int) :
            this(message, cause, domain, code, null)

    private val domain: String =
        if (domain != null) {
            if (domain.startsWith("NS")) {
                domain.substring(2)
            } else {
                domain
            }
        } else {
            CBLError.Domain.CBLITE
        }

    private val code: Int = if (code > 0) code else CBLError.Code.UNEXPECTED_ERROR

    internal actual fun getDomain(): String = domain

    internal actual fun getCode(): Int = code

    internal actual fun getInfo(): Map<String, Any?>? = info

    override fun toString(): String {
        val msg = message
        return "CouchbaseLiteException{$domain,$code,${if (msg == null) "" else "'$msg'"}}"
    }
}
