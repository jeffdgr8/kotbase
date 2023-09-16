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
 * A delegate used by the replicator to create MessageEndpointConnection objects.
 *
 * Creates an object of type MessageEndpointConnection interface.
 * An application implements the MessageEndpointConnection interface using a
 * custom transportation method such as using the NearbyConnection API
 * to exchange replication data with the endpoint.
 *
 * @param endpoint the endpoint object
 * @return the MessageEndpointConnection object
 */
public typealias MessageEndpointDelegate = (endpoint: MessageEndpoint) -> MessageEndpointConnection
