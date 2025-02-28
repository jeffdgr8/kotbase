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
 * **ENTERPRISE EDITION API**
 *
 * Authenticator for HTTP Listener password authentication
 */
public expect class ListenerPasswordAuthenticator

/**
 * Create an Authenticator using the passed delegate.
 * See [ListenerPasswordAuthenticatorDelegate]
 *
 * @param delegate where the action is.
 */
constructor(delegate: ListenerPasswordAuthenticatorDelegate) : ListenerAuthenticator
