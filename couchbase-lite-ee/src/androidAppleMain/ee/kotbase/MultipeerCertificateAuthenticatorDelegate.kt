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
 * Functional interface for custom authentication logic, allowing clients to
 * implement their own authentication strategy. Pass implementations of this
 * interface to the [MultipeerCertificateAuthenticator] to realize specific
 * authentication strategies.
 *
 * Authenticate a peer based on the passed certificates.
 * Note that the passed certificates have not been validated. All validation and
 * authorization are the responsibility of the implementation.
 *
 * @param peerId the ID of the peer being authenticated.
 * @param certs the certificate chain received from the peer.
 * @return true to accept the peer, false to reject.
 */
public typealias MultipeerCertificateAuthenticatorDelegate = (peerId: PeerInfo.PeerId, certs: List<ByteArray>) -> Boolean
