/*
 * Copyright 2023 Jeff Lockhart
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

public object Defaults {
    public object LogFile {
        /** Plaintext is not used, and instead binary encoding is used in log files  */
        public const val USE_PLAIN_TEXT: Boolean = false

        /** 512 KiB for the size of a log file  */
        public const val MAX_SIZE: Long = 524288

        /** 1 rotated file present (2 total, including the currently active log file)  */
        public const val MAX_ROTATE_COUNT: Int = 1
    }

    public object FullTextIndex {
        /** Accents and ligatures are not ignored when indexing via full text search  */
        public const val IGNORE_ACCENTS: Boolean = false
    }

    public object Replicator {
        /** Perform bidirectional replication  */
        public val TYPE: ReplicatorType = ReplicatorType.PUSH_AND_PULL

        /** One-shot replication is used, and will stop once all initial changes are processed  */
        public const val CONTINUOUS: Boolean = false

        /** A heartbeat messages is sent every 300 seconds to keep the connection alive  */
        public const val HEARTBEAT: Int = 300

        /** When replicator is not continuous, after 10 failed attempts give up on the replication  */
        public const val MAX_ATTEMPTS_SINGLE_SHOT: Int = 10

        /** When replicator is continuous, never give up unless explicitly stopped  */
        public const val MAX_ATTEMPTS_CONTINUOUS: Int = Int.MAX_VALUE

        /** Max wait time between retry attempts in seconds  */
        public const val MAX_ATTEMPT_WAIT_TIME: Int = 300

        /** Purge documents when a user loses access  */
        public const val ENABLE_AUTO_PURGE: Boolean = true

        /** Whether or not a replicator only accepts self-signed certificates from the remote  */
        public const val SELF_SIGNED_CERTIFICATE_ONLY: Boolean = false

        /** Whether or not a replicator only accepts cookies for the sender's parent domains  */
        public const val ACCEPT_PARENT_COOKIES: Boolean = false
    }

    public object Listener {
        /** No port specified, the OS will assign one  */
        public const val PORT: Int = 0

        /** TLS is enabled on the connection  */
        public const val DISABLE_TLS: Boolean = false

        /** The listener will allow database writes  */
        public const val READ_ONLY: Boolean = false

        /** Delta sync is disabled for the listener  */
        public const val ENABLE_DELTA_SYNC: Boolean = false
    }
}
