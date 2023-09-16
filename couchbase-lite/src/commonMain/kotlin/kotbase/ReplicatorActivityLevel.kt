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
 * Activity level of a replicator.
 */
public expect enum class ReplicatorActivityLevel {

    /**
     * The replication is finished or hit a fatal error.
     */
    STOPPED,

    /**
     * The replicator is offline because the remote host is unreachable.
     */
    OFFLINE,

    /**
     * The replicator is connecting to the remote host.
     */
    CONNECTING,

    /**
     * The replication is inactive; either waiting for changes or offline
     * as the remote host is unreachable.
     */
    IDLE,

    /**
     * The replication is actively transferring data.
     */
    BUSY
}
