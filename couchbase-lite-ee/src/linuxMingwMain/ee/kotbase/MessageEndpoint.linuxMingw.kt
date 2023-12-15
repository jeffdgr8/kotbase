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

public actual class MessageEndpoint
actual constructor(
    uid: String,
    target: Any?,
    protocolType: ProtocolType,
    delegate: MessageEndpointDelegate
) : Endpoint(messageEndpointUnsupported()) {

    public actual val uid: String

    public actual val target: Any?

    public actual val protocolType: ProtocolType

    public actual val delegate: MessageEndpointDelegate
}

internal fun messageEndpointUnsupported(): Nothing =
    throw UnsupportedOperationException("Message endpoint is not supported in CBL C SDK")
