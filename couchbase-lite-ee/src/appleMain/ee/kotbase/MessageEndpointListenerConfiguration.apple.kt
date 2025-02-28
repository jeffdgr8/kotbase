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

import cocoapods.CouchbaseLite.CBLMessageEndpointListenerConfiguration
import kotbase.internal.DelegatedClass
import kotbase.internal.actuals

public actual class MessageEndpointListenerConfiguration
internal constructor(
    actual: CBLMessageEndpointListenerConfiguration,
    public actual val database: Database,
    public actual val collections: Set<Collection>
) : DelegatedClass<CBLMessageEndpointListenerConfiguration>(actual) {

    @Deprecated(
        "Use MessageEndpointListener(Collection, ProtocolType)",
        ReplaceWith("MessageEndpointListener(setOf(database.defaultCollection), protocolType)")
    )
    public actual constructor(
        database: Database,
        protocolType: ProtocolType
    ) : this(
        CBLMessageEndpointListenerConfiguration(database.actual, protocolType.actual),
        database,
        setOf(database.defaultCollection)
    )

    public actual constructor(collections: Set<Collection>, protocolType: ProtocolType) : this(
        CBLMessageEndpointListenerConfiguration(collections.actuals(), protocolType.actual),
        collections.first().database,
        collections
    )

    public actual val protocolType: ProtocolType
        get() = ProtocolType.from(actual.protocolType)
}
