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
 * Log domain
 */
public expect enum class LogDomain {
    DATABASE,
    QUERY,
    REPLICATOR,
    NETWORK,
    LISTENER,
    PEER_DISCOVERY,
    MULTIPEER;

    public companion object {

        /**
         * All domains.
         */
        public val ALL: Set<LogDomain>

        @Deprecated(
            "Use LogDomain.ALL",
            ReplaceWith("LogDomain.ALL")
        )
        public val ALL_DOMAINS: Set<LogDomain>
    }
}
