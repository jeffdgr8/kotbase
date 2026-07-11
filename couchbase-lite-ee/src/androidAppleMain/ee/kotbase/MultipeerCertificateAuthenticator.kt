/*
 * Copyright 2026 Jeff Lockhart
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
 * A certificate authenticator used by the [MultipeerReplicator] to authenticate
 * a peer by verifying the peer's certificate.
 */
public expect class MultipeerCertificateAuthenticator : MultipeerAuthenticator {

    /**
     * Initialize with the root certificates to trust.
     *
     * The peer's certificate will be accepted only if it can be verified
     * using (only) one of the certificates in the passed list.
     *
     * @param rootCerts root certificates used to verify peer certificate chains.
     */
    public constructor(rootCerts: List<ByteArray>)

    /**
     * Initialize with a delegate implementing custom validation logic for verifying
     * the received certificate chain. See [MultipeerCertificateAuthenticatorDelegate].
     *
     * @param delegate The delegate to use for authentication.
     */
    public constructor(delegate: MultipeerCertificateAuthenticatorDelegate)
}
