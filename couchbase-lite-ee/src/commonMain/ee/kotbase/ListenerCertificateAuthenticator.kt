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

/**
 * A Listener Certificate Authenticator
 * Certificate base authentication and authorization.
 */
public expect class ListenerCertificateAuthenticator : ListenerAuthenticator {

    /**
     * Create an authenticator that allows clients whose certificate chains can be verified using (only)
     * on of the certs in the passed list. OS-bundled certs are ignored.
     *
     * @param rootCerts root certificates used to verify client certificate chains.
     */
    public constructor(rootCerts: List<ByteArray>)

    /**
     * Create an authenticator that delegates all responsibility for authentication and authorization
     * to the passed delegate.  See [ListenerCertificateAuthenticatorDelegate].
     *
     * @param delegate an authenticator
     */
    public constructor(delegate: ListenerCertificateAuthenticatorDelegate)
}
