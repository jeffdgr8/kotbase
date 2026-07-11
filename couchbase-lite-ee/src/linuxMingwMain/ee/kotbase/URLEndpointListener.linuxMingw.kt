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

import cnames.structs.CBLURLEndpointListener
import kotbase.internal.fleece.toKString
import kotbase.internal.wrapCBLError
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.convert
import kotlinx.cinterop.useContents
import libcblite.*
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual class URLEndpointListener
private constructor(
    actual: CPointer<CBLURLEndpointListener>,
    private val immutableConfig: ImmutableURLEndpointListenerConfiguration
) {

    private val memory = object {
        val actual = actual
    }

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        CBLURLEndpointListener_Release(it.actual)
    }

    public actual constructor(config: URLEndpointListenerConfiguration) : this(
        ImmutableURLEndpointListenerConfiguration(config)
    )

    private constructor(config: ImmutableURLEndpointListenerConfiguration) : this(
        wrapCBLError { error ->
            CBLURLEndpointListener_Create(config.actual, error)
        }!!,
        config
    )

    internal val actual: CPointer<CBLURLEndpointListener>
        get() = memory.actual

    public actual val config: URLEndpointListenerConfiguration
        get() = URLEndpointListenerConfiguration(immutableConfig)

    public actual val port: Int?
        get() {
            val port = CBLURLEndpointListener_Port(actual).toInt()
            return if (port > 0) port else null
        }

    public actual val urls: List<String>
        get() {
            val array = CBLURLEndpointListener_Urls(actual) ?: return emptyList()
            try {
                return buildList {
                    repeat(FLArray_Count(array).toInt()) { index ->
                        FLValue_AsString(FLArray_Get(array, index.convert()))
                            .toKString()?.let(::add)
                    }
                }
            } finally {
                FLMutableArray_Release(array)
            }
        }

    public actual val status: ConnectionStatus?
        get() = CBLURLEndpointListener_Status(actual).useContents {
            ConnectionStatus(connectionCount.toInt(), activeConnectionCount.toInt())
        }

    public actual val tlsIdentity: TLSIdentity?
        get() = CBLURLEndpointListener_TLSIdentity(actual)?.let {
            CBLTLSIdentity_Retain(it)!!.asTLSIdentity()
        }

    @Throws(CouchbaseLiteException::class)
    public actual fun start() {
        wrapCBLError { error ->
            CBLURLEndpointListener_Start(actual, error)
        }
    }

    public actual fun stop() {
        CBLURLEndpointListener_Stop(actual)
    }
}
