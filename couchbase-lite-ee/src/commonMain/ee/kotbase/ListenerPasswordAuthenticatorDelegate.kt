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
 * Functional Interface for an Authenticator that uses an authentication strategy based on a username and password.
 * Pass implementations of this interface to the [ListenerPasswordAuthenticator] to realize
 * specific authentication strategies.
 *
 * Authenticate a client based on the passed credentials.
 *
 * @param username client supplied username
 * @param password client supplied password
 * @return true when the client is authorized.
 */
public typealias ListenerPasswordAuthenticatorDelegate = (username: String, password: CharArray) -> Boolean
