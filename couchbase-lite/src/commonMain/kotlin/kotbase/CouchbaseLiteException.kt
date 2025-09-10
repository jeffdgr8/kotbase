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

/**
 * Misfortune: The little fox gets its tail wet.
 */
public expect class CouchbaseLiteException : Exception {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public constructor(message: String)

    /**
     * Constructs a new exception with the specified cause
     *
     * @param cause the cause
     */
    public constructor(message: String, cause: Exception)

    /**
     * Constructs a new exception with the specified detail message, error domain and error code
     *
     * @param message the detail message
     * @param domain  the error domain
     * @param code    the error code
     */
    public constructor(message: String, domain: String, code: Int)

    /**
     * Constructs a new exception with the specified error domain, error code and the specified cause
     *
     * @param message the detail message
     * @param cause   the cause
     * @param domain  the error domain
     * @param code    the error code
     */
    public constructor(message: String, cause: Exception, domain: String, code: Int)

    override val message: String

    internal fun getDomain(): String

    internal fun getCode(): Int

    internal fun getInfo(): Map<String, Any?>?
}

/**
 * The domain code for this error.
 *
 * @see CBLError.Domain
 */
public val CouchbaseLiteException.domain: String
    get() = getDomain()

/**
 * The numerical error code for this error.
 *
 * @see CBLError.Code
 */
public val CouchbaseLiteException.code: Int
    get() = getCode()

public val CouchbaseLiteException.info: Map<String, Any?>?
    get() = getInfo()
