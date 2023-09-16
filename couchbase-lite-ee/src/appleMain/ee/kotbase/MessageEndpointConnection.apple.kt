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

import cocoapods.CouchbaseLite.CBLMessage
import cocoapods.CouchbaseLite.CBLMessageEndpointConnectionProtocol
import cocoapods.CouchbaseLite.CBLMessagingError
import cocoapods.CouchbaseLite.CBLReplicatorConnectionProtocol
import kotbase.ext.toException
import platform.Foundation.NSError
import platform.darwin.NSObject

internal fun MessageEndpointConnection.convert(): CBLMessageEndpointConnectionProtocol =
    NativeMessageEndpointConnection(this)

internal class NativeMessageEndpointConnection(
    internal val original: MessageEndpointConnection
) : NSObject(), CBLMessageEndpointConnectionProtocol {

    override fun open(
        connection: CBLReplicatorConnectionProtocol,
        completion: (Boolean, CBLMessagingError?) -> Unit
    ) {
        original.open(connection.convert(), completion.convert())
    }

    override fun close(error: NSError?, completion: () -> Unit) {
        original.close(error?.toException(), completion)
    }

    override fun send(message: CBLMessage, completion: (Boolean, CBLMessagingError?) -> Unit) {
        original.send(Message(message), completion.convert())
    }
}
