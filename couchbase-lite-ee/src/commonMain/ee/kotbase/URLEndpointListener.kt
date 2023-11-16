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
 * A listener to which remote replicators can connect.
 */
public expect class URLEndpointListener

/**
 * Create a URLEndpointListener with the passed configuration.
 *
 * @param config the listener configuration.
 */
constructor(config: URLEndpointListenerConfiguration) {

    /**
     * The listener's configuration (read only).
     */
    public val config: URLEndpointListenerConfiguration

    /**
     * Get the listener's port.
     * This method will return null except between the time
     * the listener is started and the time it is stopped.
     *
     * When a listener is configured with the port number 0, the return value from this function will
     * give the port at which the listener is actually listening.
     *
     * @return the listener's port, or null.
     */
    public val port: Int?

    /**
     * Get the list of URIs for the listener.
     *
     * @return a list of listener URIs.
     */
    public val urls: List<String>

    /**
     * The listener status.
     */
    public val status: ConnectionStatus?

    /**
     * The TLS identity used by the listener.
     */
    public val tlsIdentity: TLSIdentity?

    /**
     * Start the listener.
     */
    @Throws(CouchbaseLiteException::class)
    public fun start()

    /**
     * Stop the listener.
     */
    public fun stop()
}
