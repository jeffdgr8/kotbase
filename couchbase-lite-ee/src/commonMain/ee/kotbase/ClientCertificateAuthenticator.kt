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
 * An authenticator for client certificate authentication which happens during
 * the TLS handshake when connecting to a server.
 *
 * The client certificate authenticator is currently used only for authenticating to
 * a URLEndpointListener.  The URLEndpointListener must have TLS enabled and
 * must be configured with a ListenerCertificateAuthenticator to verify client
 * certificates.
 *
 * @constructor Creates a ClientCertificateAuthenticator object with the given client identity.
 *
 * @param identity client identity
 */
public expect class ClientCertificateAuthenticator(identity: TLSIdentity) : Authenticator {

    /**
     * The client identity.
     */
    public val identity: TLSIdentity
}
