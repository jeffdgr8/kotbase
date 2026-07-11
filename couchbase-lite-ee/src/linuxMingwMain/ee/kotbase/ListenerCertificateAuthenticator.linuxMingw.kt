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

import cnames.structs.CBLCert
import kotbase.internal.fleece.toByteArray
import kotbase.internal.wrapCBLError
import kotbase.util.to
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.convert
import kotlinx.cinterop.cValue
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.staticCFunction
import libcblite.CBLCert_CreateWithData
import libcblite.CBLCert_Data
import libcblite.CBLCert_Release
import libcblite.CBLListenerAuth_CreateCertificate
import libcblite.CBLListenerAuth_CreateCertificateWithRootCerts
import libcblite.FLSlice
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual class ListenerCertificateAuthenticator
private constructor(
    actual: CPointer<cnames.structs.CBLListenerAuthenticator>,
    ref: StableRef<*>? = null,
    rootCerts: CPointer<CBLCert>? = null
) : ListenerAuthenticator(actual, ref) {

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val certCleaner = createCleaner(rootCerts) { certs ->
        certs?.let { CBLCert_Release(it) }
    }

    public actual constructor(rootCerts: List<ByteArray>) : this(rootCerts.toCBLCertChain())

    private constructor(rootCerts: CPointer<CBLCert>) : this(
        CBLListenerAuth_CreateCertificateWithRootCerts(rootCerts)!!,
        rootCerts = rootCerts
    )

    public actual constructor(delegate: ListenerCertificateAuthenticatorDelegate) : this(
        StableRef.create(delegate)
    )

    private constructor(ref: StableRef<ListenerCertificateAuthenticatorDelegate>) : this(
        CBLListenerAuth_CreateCertificate(
            staticCFunction { context, cert ->
                context.to<ListenerCertificateAuthenticatorDelegate>().invoke(
                    cert?.toCertsData() ?: emptyList()
                )
            },
            ref.asCPointer()
        )!!,
        ref
    )
}

private fun ByteArray.toCBLCert(): CPointer<CBLCert> = memScoped {
    val data = this@toCBLCert
    val slice = cValue<FLSlice> {
        buf = if (data.isEmpty()) null else data.refTo(0).getPointer(this@memScoped)
        size = data.size.convert()
    }
    wrapCBLError { error ->
        CBLCert_CreateWithData(slice, error)
    }!!
}

/**
 * Builds a single CBLCert containing all of the given certificates (each in DER or
 * PEM format) by concatenating their PEM representations: PEM data consisting of a
 * series of certificates produces a CBLCert chain (see CBLTLSIdentity.h).
 */
private fun List<ByteArray>.toCBLCertChain(): CPointer<CBLCert> {
    require(isNotEmpty()) { "rootCerts must contain at least one certificate" }
    if (size == 1) return first().toCBLCert()
    val pem = joinToString("") { certData ->
        val cert = certData.toCBLCert()
        try {
            CBLCert_Data(cert, true).toByteArray().decodeToString()
        } finally {
            CBLCert_Release(cert)
        }
    }
    return pem.encodeToByteArray().toCBLCert()
}
