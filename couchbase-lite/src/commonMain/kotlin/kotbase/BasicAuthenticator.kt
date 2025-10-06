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
 * The BasicAuthenticator class is an authenticator that will authenticate using HTTP Basic
 * auth with the given username and password. This should only be used over an SSL/TLS connection,
 * as otherwise it's very easy for anyone sniffing network traffic to read the password.
 *
 * @constructor Create a Basic Authenticator.
 * The new instance contains a copy of the password char[] parameter:
 * the owner of the original retains the responsibility for zeroing it before releasing it.
 */
public expect class BasicAuthenticator(username: String, password: CharArray) : Authenticator {

    public val username: String

    /**
     * Get the password.
     * The returned char[] is a copy: the owner is responsible for zeroing it before releasing it.
     *
     * @return the password, as a char[].
     */
    public val passwordChars: CharArray
}
