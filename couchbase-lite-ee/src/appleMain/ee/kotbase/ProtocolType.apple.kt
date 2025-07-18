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

import cocoapods.CouchbaseLite.CBLProtocolType
import cocoapods.CouchbaseLite.CBLProtocolType.kCBLProtocolTypeByteStream
import cocoapods.CouchbaseLite.CBLProtocolType.kCBLProtocolTypeMessageStream

public actual enum class ProtocolType {
    MESSAGE_STREAM,
    BYTE_STREAM;

    internal val actual: CBLProtocolType
        get() = when (this) {
            MESSAGE_STREAM -> kCBLProtocolTypeMessageStream
            BYTE_STREAM -> kCBLProtocolTypeByteStream
        }

    internal companion object {

        @Suppress("REDUNDANT_ELSE_IN_WHEN")
        internal fun from(protocolType: CBLProtocolType): ProtocolType = when (protocolType) {
            kCBLProtocolTypeMessageStream -> MESSAGE_STREAM
            kCBLProtocolTypeByteStream -> BYTE_STREAM
            else -> error("Unexpected CBLProtocolType ($protocolType)")
        }
    }
}
