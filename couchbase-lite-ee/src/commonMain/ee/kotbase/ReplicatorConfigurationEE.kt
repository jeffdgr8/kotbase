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
 * **ENTERPRISE EDITION API**
 *
 * Specify whether the replicator will accept any and only self-signed certificates.
 * Any non-self-signed certificates will be rejected to avoid accidentally using
 * this mode with the non-self-signed certs in production. The default value is false.
 *
 * @param acceptOnlySelfSignedServerCertificate Whether the replicator will accept
 * any and only self-signed certificates.
 * @return this.
 */
public fun ReplicatorConfiguration.setAcceptOnlySelfSignedServerCertificate(
    acceptOnlySelfSignedServerCertificate: Boolean
): ReplicatorConfiguration {
    isAcceptOnlySelfSignedServerCertificate = acceptOnlySelfSignedServerCertificate
    return this
}

/**
 * **ENTERPRISE EDITION API**
 *
 * Whether the replicator will accept any and only self-signed server certificates.
 */
public expect var ReplicatorConfiguration.isAcceptOnlySelfSignedServerCertificate: Boolean
