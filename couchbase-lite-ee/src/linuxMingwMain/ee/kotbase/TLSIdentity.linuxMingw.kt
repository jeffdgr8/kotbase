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
import cnames.structs.CBLTLSIdentity
import kotbase.internal.fleece.toByteArray
import kotbase.internal.fleece.toFLString
import kotbase.internal.wrapCBLError
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.readValue
import libcblite.*
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

public actual class TLSIdentity
internal constructor(actual: CPointer<CBLTLSIdentity>) {

    private val memory = object {
        val actual = actual
    }

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        CBLTLSIdentity_Release(it.actual)
    }

    internal val actual: CPointer<CBLTLSIdentity>
        get() = memory.actual

    public actual val certs: List<ByteArray>
        get() = CBLTLSIdentity_Certificates(actual)?.toCertsData() ?: emptyList()

    public actual val expiration: Instant
        get() = Instant.fromEpochMilliseconds(CBLTLSIdentity_Expiration(actual))

    public actual companion object {

        @Throws(CouchbaseLiteException::class)
        public actual fun getIdentity(alias: String): TLSIdentity? =
            getTLSIdentity(alias)

        @Throws(CouchbaseLiteException::class)
        public actual fun createIdentity(
            usage: Set<KeyUsage>,
            attributes: Map<String, String>,
            expiration: Instant?,
            alias: String
        ): TLSIdentity = createTLSIdentity(usage, attributes, expiration, alias)

        @Throws(CouchbaseLiteException::class)
        public actual fun deleteIdentity(alias: String) {
            deleteTLSIdentity(alias)
        }

        /**
         * Create an anonymous, self-signed TLS identity with the given certificate
         * attributes. The identity is not persisted and lives only as long as the
         * returned object.
         *
         * This is the only way to construct a [TLSIdentity] on Linux, which has no
         * standard secure key storage for persisted, alias-based identities.
         *
         * @param usage      certificate usage: server or client
         * @param attributes certificate attributes; the Common Name
         * ([CERT_ATTRIBUTE_COMMON_NAME]) attribute is required
         * @param expiration expiration date: default is one year
         * @return the new identity
         * @throws CouchbaseLiteException on failure to create the identity
         */
        @Throws(CouchbaseLiteException::class)
        public fun createAnonymousIdentity(
            usage: Set<KeyUsage>,
            attributes: Map<String, String>,
            expiration: Instant? = null
        ): TLSIdentity = createTLSIdentity(usage, attributes, expiration, null)
    }
}

internal fun CPointer<CBLTLSIdentity>.asTLSIdentity(): TLSIdentity =
    TLSIdentity(this)

/**
 * Returns the DER data of each certificate in the chain, starting with this one.
 *
 * The receiver is a borrowed reference; references returned by
 * CBLCert_CertNextInChain are owned and released here.
 */
internal fun CPointer<CBLCert>.toCertsData(): List<ByteArray> = buildList {
    var cert: CPointer<CBLCert>? = this@toCertsData
    var owned = false
    while (cert != null) {
        add(CBLCert_Data(cert, false).toByteArray())
        val next = CBLCert_CertNextInChain(cert)
        if (owned) CBLCert_Release(cert)
        cert = next
        owned = true
    }
}

/**
 * Creates a self-signed identity. A non-null [label] persists the identity in the
 * platform's secure key storage on Windows (CNG Key Storage Provider). Linux has no
 * secure key storage, so the C SDK returns an error for a non-null label there
 * (see CBLTLSIdentity.h), which is propagated as a [CouchbaseLiteException].
 * A null label creates an anonymous, non-persisted identity on both platforms.
 */
internal fun createTLSIdentity(
    usage: Set<KeyUsage>,
    attributes: Map<String, String>,
    expiration: Instant?,
    label: String?
): TLSIdentity {
    require(attributes.containsKey(TLSIdentity.CERT_ATTRIBUTE_COMMON_NAME)) {
        "The Common Name (CN) certificate attribute is required"
    }
    val attrs = attributes.toCertAttributes()
    try {
        return wrapCBLError { error ->
            memScoped {
                CBLTLSIdentity_CreateIdentity(
                    usage.toCBLKeyUsages(),
                    attrs,
                    expiration?.let { (it - Clock.System.now()).inWholeMilliseconds }
                        ?: DEFAULT_CERT_VALIDITY.inWholeMilliseconds,
                    label.toFLString(this),
                    error
                )
            }
        }!!.asTLSIdentity()
    } finally {
        FLMutableDict_Release(attrs)
    }
}

// Certificate validity when no expiration is given,
// matching the 1-year default of the other platforms
private val DEFAULT_CERT_VALIDITY = 365.days

/**
 * Translates kotbase's common certificate attribute keys
 * (TLSIdentity.CERT_ATTRIBUTE_*) to the CBL C SDK's attribute keys
 * (kCBLCertAttrKey*). Most are identical strings; e.g. "locality" differs ("L").
 */
private fun Map<String, String>.toCertAttributes(): FLMutableDict {
    val attributes = FLMutableDict_New()!!
    forEach { (key, value) ->
        memScoped {
            val attrKey = certAttributeKeys[key] ?: key.toFLString(this)
            FLMutableDict_SetString(attributes, attrKey, value.toFLString(this))
        }
    }
    return attributes
}

private val certAttributeKeys: Map<String, CValue<FLString>> by lazy {
    mapOf(
        TLSIdentity.CERT_ATTRIBUTE_COMMON_NAME to kCBLCertAttrKeyCommonName.readValue(),
        TLSIdentity.CERT_ATTRIBUTE_PSEUDONYM to kCBLCertAttrKeyPseudonym.readValue(),
        TLSIdentity.CERT_ATTRIBUTE_GIVEN_NAME to kCBLCertAttrKeyGivenName.readValue(),
        TLSIdentity.CERT_ATTRIBUTE_SURNAME to kCBLCertAttrKeySurname.readValue(),
        TLSIdentity.CERT_ATTRIBUTE_ORGANIZATION to kCBLCertAttrKeyOrganization.readValue(),
        TLSIdentity.CERT_ATTRIBUTE_ORGANIZATION_UNIT to kCBLCertAttrKeyOrganizationUnit.readValue(),
        TLSIdentity.CERT_ATTRIBUTE_POSTAL_ADDRESS to kCBLCertAttrKeyPostalAddress.readValue(),
        TLSIdentity.CERT_ATTRIBUTE_LOCALITY to kCBLCertAttrKeyLocality.readValue(),
        TLSIdentity.CERT_ATTRIBUTE_POSTAL_CODE to kCBLCertAttrKeyPostalCode.readValue(),
        TLSIdentity.CERT_ATTRIBUTE_STATE_OR_PROVINCE to kCBLCertAttrKeyStateOrProvince.readValue(),
        TLSIdentity.CERT_ATTRIBUTE_COUNTRY to kCBLCertAttrKeyCountry.readValue(),
        TLSIdentity.CERT_ATTRIBUTE_EMAIL_ADDRESS to kCBLCertAttrKeyEmailAddress.readValue(),
        TLSIdentity.CERT_ATTRIBUTE_HOSTNAME to kCBLCertAttrKeyHostname.readValue(),
        TLSIdentity.CERT_ATTRIBUTE_URL to kCBLCertAttrKeyURL.readValue(),
        TLSIdentity.CERT_ATTRIBUTE_IP_ADDRESS to kCBLCertAttrKeyIPAddress.readValue(),
        TLSIdentity.CERT_ATTRIBUTE_REGISTERED_ID to kCBLCertAttrKeyRegisteredID.readValue()
    )
}

/**
 * Gets an identity persisted with the given label from the platform's secure key
 * storage. Supported on Windows (CNG Key Storage Provider) only: the CBL C SDK
 * does not support persisted identities on Linux (see CBLTLSIdentity.h), where a
 * [CouchbaseLiteException] is thrown.
 */
internal expect fun getTLSIdentity(label: String): TLSIdentity?

/**
 * Deletes the identity persisted with the given label from the platform's secure
 * key storage. Supported on Windows (CNG Key Storage Provider) only: the CBL C SDK
 * does not support persisted identities on Linux (see CBLTLSIdentity.h), where a
 * [CouchbaseLiteException] is thrown.
 */
internal expect fun deleteTLSIdentity(label: String)
