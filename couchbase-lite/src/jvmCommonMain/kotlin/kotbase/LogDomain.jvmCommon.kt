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

import com.couchbase.lite.LogDomain as CBLLogDomain

public actual enum class LogDomain {
    DATABASE,
    QUERY,
    REPLICATOR,
    NETWORK,
    LISTENER;

    internal val actual: CBLLogDomain
        get() = when (this) {
            DATABASE -> CBLLogDomain.DATABASE
            QUERY -> CBLLogDomain.QUERY
            REPLICATOR -> CBLLogDomain.REPLICATOR
            NETWORK -> CBLLogDomain.NETWORK
            LISTENER -> CBLLogDomain.LISTENER
        }

    public actual companion object {

        public actual val ALL: Set<LogDomain> = entries.toSet()

        @Deprecated(
            "Use LogDomain.ALL",
            ReplaceWith("LogDomain.ALL")
        )
        public actual val ALL_DOMAINS: Set<LogDomain>
            get() = ALL

        internal fun from(logDomain: CBLLogDomain): LogDomain = when (logDomain) {
            CBLLogDomain.DATABASE -> DATABASE
            CBLLogDomain.QUERY -> QUERY
            CBLLogDomain.REPLICATOR -> REPLICATOR
            CBLLogDomain.NETWORK -> NETWORK
            CBLLogDomain.LISTENER -> LISTENER
        }
    }
}
