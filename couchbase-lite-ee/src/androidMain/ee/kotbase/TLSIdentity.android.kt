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

import com.couchbase.lite.deleteTLSIdentity
import kotbase.internal.DelegatedClass
import kotbase.ext.toByteArray
import kotbase.ext.toDate
import kotbase.ext.toKotlinInstant
import kotlinx.datetime.Instant
import com.couchbase.lite.TLSIdentity as CBLTLSIdentity

public actual class TLSIdentity
internal constructor(actual: CBLTLSIdentity) : DelegatedClass<CBLTLSIdentity>(actual) {

    public actual val certs: List<ByteArray>
        get() = actual.certs.map { it.toByteArray() }

    public actual val expiration: Instant
        get() = actual.expiration.toKotlinInstant()

    public actual companion object {

        @Throws(CouchbaseLiteException::class)
        public actual fun getIdentity(alias: String): TLSIdentity? =
            CBLTLSIdentity.getIdentity(alias)?.asTLSIdentity()

        @Throws(CouchbaseLiteException::class)
        public actual fun createIdentity(
            isServer: Boolean,
            attributes: Map<String, String>,
            expiration: Instant?,
            alias: String
        ): TLSIdentity = CBLTLSIdentity.createIdentity(
            isServer,
            attributes,
            expiration?.toDate(),
            alias
        ).asTLSIdentity()

        @Throws(CouchbaseLiteException::class)
        public actual fun deleteIdentity(alias: String) {
            deleteTLSIdentity(alias)
        }
    }
}

internal actual val TLSIdentity.actual: CBLTLSIdentity
    get() = actual

internal actual fun CBLTLSIdentity.asTLSIdentity(): TLSIdentity =
    TLSIdentity(this)
