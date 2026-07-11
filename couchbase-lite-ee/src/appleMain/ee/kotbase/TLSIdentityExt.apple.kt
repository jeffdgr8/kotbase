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

import cocoapods.CouchbaseLite.CBLTLSIdentity
import kotbase.ext.toNSData
import kotbase.ext.wrapCBLError
import kotlin.time.Instant
import kotlinx.datetime.toNSDate

@Throws(CouchbaseLiteException::class)
public actual fun TLSIdentity.Companion.createdSignedIdentityInsecure(
    usage: Set<KeyUsage>,
    attributes: Map<String, String>,
    caKey: ByteArray,
    caCertificate: ByteArray,
    expiration: Instant?,
    alias: String
): TLSIdentity {
    return wrapCBLError { error ->
        @Suppress("UNCHECKED_CAST")
        CBLTLSIdentity.createSignedIdentityInsecureForKeyUsages(
            usage.actual,
            attributes as Map<Any?, *>,
            expiration?.toNSDate(),
            caKey.toNSData(),
            caCertificate.toNSData(),
            alias,
            error
        )
    }!!.asTLSIdentity()
}
