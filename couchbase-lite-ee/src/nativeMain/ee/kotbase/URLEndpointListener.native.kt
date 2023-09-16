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

public actual class URLEndpointListener
actual constructor(config: URLEndpointListenerConfiguration) {

    init {
        urlEndpointListenerUnsupported()
    }

    public actual val config: URLEndpointListenerConfiguration

    public actual val port: Int?

    public actual val urls: List<String>

    public actual val status: ConnectionStatus?

    public actual val tlsIdentity: TLSIdentity?

    @Throws(CouchbaseLiteException::class)
    public actual fun start() {
    }

    public actual fun stop() {
    }
}

internal fun urlEndpointListenerUnsupported(): Nothing =
    throw UnsupportedOperationException("URL endpoint listener is not supported in CBL C SDK")
