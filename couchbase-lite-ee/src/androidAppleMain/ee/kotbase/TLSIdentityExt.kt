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

import kotlin.time.Instant

/**
 * **ENTERPRISE EDITION API**
 *
 * Create a certificate signed by the provided CA. This is inherently insecure and should
 * only be used in environments that are meant for testing, or environments that are
 * otherwise secured against intruders by other mechanisms.
 *
 * The created identity key will be stored in the secure storage using the specified alias,
 * while neither the CA certificate nor the CA signing key will be stored anywhere.
 * The identity can be recovered using the provided alias, after this method returns.
 *
 * @param usage         certificate usage: server or client
 * @param attributes    certificate attributes
 * @param caKey         The PEM or DER data of the CA signing key
 * @param caCertificate The PEM or DER data of the CA certificate
 * @param expiration    expiration date
 * @param alias         alias used to identify the key/certificate entry, in the keystore
 * @return the new identity
 * @throws CouchbaseLiteException on failure to create
 */
@Throws(CouchbaseLiteException::class)
public expect fun TLSIdentity.Companion.createdSignedIdentityInsecure(
    usage: Set<KeyUsage>,
    attributes: Map<String, String>,
    caKey: ByteArray,
    caCertificate: ByteArray,
    expiration: Instant?,
    alias: String
): TLSIdentity
