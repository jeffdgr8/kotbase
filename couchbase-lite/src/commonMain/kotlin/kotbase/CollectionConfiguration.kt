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

/**
 * The collection configuration that can be configured specifically for the replication.
 */
public expect class CollectionConfiguration(
    channels: List<String>? = null,
    documentIDs: List<String>? = null,
    pullFilter: ReplicationFilter? = null,
    pushFilter: ReplicationFilter? = null,
    conflictResolver: ConflictResolver? = null
) {

    /**
     * Sets a collection of document IDs to filter by: if given, only documents
     * with these IDs will be pushed and/or pulled.
     *
     * @param documentIDs The document IDs.
     * @return this.
     */
    public fun setDocumentIDs(documentIDs: List<String>?): CollectionConfiguration

    /**
     * Sets a collection of Sync Gateway channel names from which to pull Documents.
     * If unset, all accessible channels will be pulled.
     * Default is empty: pull from all accessible channels.
     *
     * Note:  Channel specifications apply only to replications
     * pulling from a SyncGateway and only the channels visible
     * to the authenticated user.  Channel specs are ignored:
     *
     *  * during a push replication.
     *  * during peer-to-peer or database-to-database replication
     *  * when the specified channel is not accessible to the user
     *
     * @param channels The Sync Gateway channel names.
     * @return this.
     */
    public fun setChannels(channels: List<String>?): CollectionConfiguration

    /**
     * Sets the conflict resolver.
     *
     * @param conflictResolver A conflict resolver.
     * @return this.
     */
    public fun setConflictResolver(conflictResolver: ConflictResolver?): CollectionConfiguration

    /**
     * Sets a filter object for validating whether the documents can be pulled from the
     * remote endpoint. Only documents for which the object returns true are replicated.
     *
     * @param pullFilter The filter to filter the document to be pulled.
     * @return this.
     */
    public fun setPullFilter(pullFilter: ReplicationFilter?): CollectionConfiguration

    /**
     * Sets a filter object for validating whether the documents can be pushed
     * to the remote endpoint.
     *
     * @param pushFilter The filter to filter the document to be pushed.
     * @return this.
     */
    public fun setPushFilter(pushFilter: ReplicationFilter?): CollectionConfiguration

    /**
     * A collection of Sync Gateway channel names from which to pull Documents.
     * If unset, all accessible channels will be pulled.
     * Default is empty: pull from all accessible channels.
     *
     * Note:  Channel specifications apply only to replications
     * pulling from a SyncGateway and only the channels visible
     * to the authenticated user.  Channel specs are ignored:
     *
     *  * during a push replication.
     *  * during peer-to-peer or database-to-database replication
     *  * when the specified channel is not accessible to the user
     */
    public var channels: List<String>?

    /**
     * A collection of document IDs to filter: if not null, only documents with these IDs will be pushed
     * and/or pulled.
     */
    public var documentIDs: List<String>?

    /**
     * The conflict resolver.
     */
    public var conflictResolver: ConflictResolver?

    /**
     * The filter used to determine whether a document will be pulled
     * from the remote endpoint.
     */
    public var pullFilter: ReplicationFilter?

    /**
     * The filter used to determine whether a document will be pushed
     * to the remote endpoint.
     */
    public var pushFilter: ReplicationFilter?
}
