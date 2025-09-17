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
 * Configuration for MessageEndpointListener
 */
public expect class MessageEndpointListenerConfiguration {

    /**
     * The database to which the listener is attached
     *
     * @param database     to which the listener is attached
     * @param protocolType protocol type
     */
    @Deprecated(
        "Use MessageEndpointListener(Collection, ProtocolType)",
        ReplaceWith("MessageEndpointListener(setOf(database.defaultCollection), protocolType)")
    )
    public constructor(database: Database, protocolType: ProtocolType)

    /**
     * Create a MessageEndpointListenerConfiguration with the passed protocol type, for the passed Collections
     * The passed set must contain at least one collection and all the collections it contains must belong
     * to the same scope and the same database, otherwise an InvalidArgumentException will be thrown.
     * If one of the specified collections is deleted during replication, connected clients will be closed
     * with an error.
     *
     * @param collections  the collections to which the listener is attached
     * @param protocolType protocol type
     */
    public constructor(collections: Set<Collection>, protocolType: ProtocolType)

    /**
     * The endpoint database
     */
    @Deprecated(
        "Use collections",
        ReplaceWith("collections")
    )
    public val database: Database

    public val collections: Set<Collection>

    public val protocolType: ProtocolType
}
