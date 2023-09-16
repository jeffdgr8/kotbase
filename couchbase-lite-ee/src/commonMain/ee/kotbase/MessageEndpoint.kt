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
 * Message endpoint.
 */
public expect class MessageEndpoint

/**
 * Initializes a MessageEndpoint object.
 *
 * @param uid          the unique identifier of the endpoint
 * @param target       an optional arbitrary object that represents the endpoint
 * @param protocolType the data transportation protocol
 * @param delegate     the delegate for creating MessageEndpointConnection objects
 */
constructor(
    uid: String,
    target: Any?,
    protocolType: ProtocolType,
    delegate: MessageEndpointDelegate
) : Endpoint {

    /**
     * The unique identifier of the endpoint.
     */
    public val uid: String

    /**
     * The target object which is an arbitrary object that represents the endpoint.
     */
    public val target: Any?

    /**
     * Gets the data transportation protocol of the endpoint.
     *
     * @return the data transportation protocol
     */
    public val protocolType: ProtocolType

    /**
     * Gets the delegate object used for creating MessageEndpointConnection objects.
     *
     * @return the delegate object.
     */
    public val delegate: MessageEndpointDelegate
}
