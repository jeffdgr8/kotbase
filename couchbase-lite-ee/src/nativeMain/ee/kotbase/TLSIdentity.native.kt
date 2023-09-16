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

import kotlinx.datetime.Instant

public actual class TLSIdentity {

    public actual val certs: List<ByteArray>
        get() = emptyList()

    public actual val expiration: Instant
        get() = urlEndpointListenerUnsupported()

    public actual companion object {

        @Throws(CouchbaseLiteException::class)
        public actual fun getIdentity(alias: String): TLSIdentity? = null

        @Throws(CouchbaseLiteException::class)
        public actual fun createIdentity(
            isServer: Boolean,
            attributes: Map<String, String>,
            expiration: Instant?,
            alias: String
        ): TLSIdentity {
            urlEndpointListenerUnsupported()
        }

        @Throws(CouchbaseLiteException::class)
        public actual fun deleteIdentity(alias: String) {
        }
    }
}
