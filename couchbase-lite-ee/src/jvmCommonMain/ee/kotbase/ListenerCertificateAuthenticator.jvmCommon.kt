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

import kotbase.ext.toCertificates
import com.couchbase.lite.ListenerCertificateAuthenticator as CBLListenerCertificateAuthenticator

public actual class ListenerCertificateAuthenticator
internal constructor(actual: CBLListenerCertificateAuthenticator) : ListenerAuthenticator(actual) {

    public actual constructor(rootCerts: List<ByteArray>) : this(
        CBLListenerCertificateAuthenticator(rootCerts.toCertificates())
    )

    public actual constructor(delegate: ListenerCertificateAuthenticatorDelegate) : this(
        CBLListenerCertificateAuthenticator(delegate.convert())
    )
}

internal val ListenerCertificateAuthenticator.actual: CBLListenerCertificateAuthenticator
    get() = platformState!!.actual as CBLListenerCertificateAuthenticator