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

public actual class MessageEndpointListenerConfiguration
internal constructor(
    public actual val database: Database,
    public actual val collections: Set<Collection>,
    public actual val protocolType: ProtocolType
) {

    @Deprecated(
        "Use MessageEndpointListener(Collection, ProtocolType)",
        ReplaceWith("MessageEndpointListener(setOf(database.getDefaultCollection()!!), protocolType)")
    )
    public actual constructor(
        database: Database,
        protocolType: ProtocolType
    ) : this(
        database,
        setOf(database.getDefaultCollectionNotNull()),
        protocolType
    )

    public actual constructor(
        collections: Set<Collection>,
        protocolType: ProtocolType
    ) : this(
        collections.first().database,
        collections,
        protocolType
    )

    init {
        messageEndpointUnsupported()
    }
}
