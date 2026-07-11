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

import kotbase.ext.toDate
import kotlin.time.Instant
import com.couchbase.lite.TLSIdentity as CBLTLSIdentity

@Throws(CouchbaseLiteException::class)
public actual fun TLSIdentity.Companion.createdSignedIdentityInsecure(
    usage: Set<KeyUsage>,
    attributes: Map<String, String>,
    caKey: ByteArray,
    caCertificate: ByteArray,
    expiration: Instant?,
    alias: String
): TLSIdentity = CBLTLSIdentity.createdSignedIdentityInsecure(
    usage,
    attributes,
    caKey,
    caCertificate,
    expiration?.toDate(),
    alias
).asTLSIdentity()
