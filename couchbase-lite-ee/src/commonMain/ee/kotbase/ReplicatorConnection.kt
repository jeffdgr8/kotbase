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
 * The replicator connection used by the application to tell the replicator to
 * consume the data received from the other peer or to close the connection.
 */
public interface ReplicatorConnection {

    /**
     * Tells the replicator to close the current replicator connection. In return,
     * the replicator will call the MessageEndpointConnection's close(error, completion)
     * to acknowledge the closed connection.
     *
     * @param error the error if any
     */
    public fun close(error: MessagingError?)

    /**
     * Tells the replicator to consume the data received from the other peer.
     *
     * @param message the message
     */
    public fun receive(message: Message)
}
