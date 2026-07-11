/*
 * Copyright 2026 Jeff Lockhart
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
 * The multipeer replicator's status.
 */
public expect class MultipeerReplicatorStatus {

    /**
     * The transport the status is reported for, or null if the status is an aggregate
     * across all transports.
     */
    public val transport: MultipeerTransport?

    /**
     * Whether the multipeer replicator is active or not.
     * This means the replicator is discovering peers or performing replication.
     *
     * Note that this value is approximate.
     */
    public val isActive: Boolean

    /**
     * The error encountered, if any.
     */
    public val error: CouchbaseLiteException?
}
