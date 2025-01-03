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

import com.couchbase.lite.MessageEndpoint as CBLMessageEndpoint

public actual class MessageEndpoint
internal constructor(
    override val actual: CBLMessageEndpoint,
    public actual val delegate: MessageEndpointDelegate
) : Endpoint(actual) {

    public actual constructor(
        uid: String,
        target: Any?,
        protocolType: ProtocolType,
        delegate: MessageEndpointDelegate
    ) : this(
        CBLMessageEndpoint(uid, target, protocolType, delegate.convert()),
        delegate
    )

    public actual val uid: String
        get() = actual.uid

    public actual val target: Any?
        get() = actual.target

    public actual val protocolType: ProtocolType
        get() = actual.protocolType
}
