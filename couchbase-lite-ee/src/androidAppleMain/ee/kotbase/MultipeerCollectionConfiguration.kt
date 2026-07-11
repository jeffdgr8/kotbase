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
 * A configuration for a collection in a multipeer environment.
 *
 * This class allows you to configure document IDs to filter, push and pull filters,
 * and a custom conflict resolver for an associated collection.
 *
 * @constructor Creates a new configuration instance for the given collection.
 *
 * @param collection The [Collection] instance to replicate.
 */
public class MultipeerCollectionConfiguration(

    /**
     * The collection this configuration replicates.
     */
    public val collection: Collection
) {

    internal constructor(config: MultipeerCollectionConfiguration) : this(config.collection) {
        documentIDs = config.documentIDs
        pushFilter = config.pushFilter
        pullFilter = config.pullFilter
        conflictResolver = config.conflictResolver
    }

    /**
     * Sets a collection of document IDs to filter by: if given, only documents
     * with these IDs will be pushed and/or pulled.
     *
     * @param documentIDs The document IDs.
     * @return this.
     */
    public fun setDocumentIDs(documentIDs: List<String>?): MultipeerCollectionConfiguration {
        this.documentIDs = documentIDs
        return this
    }

    /**
     * Sets a filter object for validating whether the documents can be pushed
     * to the remote peer. Only documents for which the function returns true
     * are replicated.
     *
     * @param pushFilter The filter to filter the document to be pushed.
     * @return this.
     */
    public fun setPushFilter(pushFilter: MultipeerReplicationFilter?): MultipeerCollectionConfiguration {
        this.pushFilter = pushFilter
        return this
    }

    /**
     * Sets a filter object for validating whether the documents can be pulled from the
     * remote peer. Only documents for which the function returns true are replicated.
     *
     * @param pullFilter The filter to filter the document to be pulled.
     * @return this.
     */
    public fun setPullFilter(pullFilter: MultipeerReplicationFilter?): MultipeerCollectionConfiguration {
        this.pullFilter = pullFilter
        return this
    }

    /**
     * Sets the custom conflict resolver. If not specified, the default conflict
     * resolver will be used.
     *
     * @param conflictResolver A conflict resolver.
     * @return this.
     */
    public fun setConflictResolver(conflictResolver: MultipeerConflictResolver?): MultipeerCollectionConfiguration {
        this.conflictResolver = conflictResolver
        return this
    }

    /**
     * A collection of document IDs to filter (possibly null): if not null, only
     * documents with these IDs will be pushed and/or pulled.
     */
    public var documentIDs: List<String>? = null
        set(value) {
            if (value.isNullOrEmpty()) {
                field = null
                return
            }
            value.forEach {
                require(it.isNotEmpty()) { "Document Id cannot be null or empty" }
            }
            field = value.toList()
        }

    /**
     * The filter used to determine whether a document will be pushed
     * to the remote peer (possibly null).
     */
    public var pushFilter: MultipeerReplicationFilter? = null

    /**
     * The filter used to determine whether a document will be pulled
     * from the remote peer (possibly null).
     */
    public var pullFilter: MultipeerReplicationFilter? = null

    /**
     * The custom conflict resolver (possibly null).
     */
    public var conflictResolver: MultipeerConflictResolver? = null

    public companion object {

        /**
         * Creates a set of [MultipeerCollectionConfiguration] instances from the given collections.
         *
         * Each specified collection will be wrapped in a [MultipeerCollectionConfiguration]
         * using default settings (no filters and no custom conflict resolvers).
         *
         * This is a convenience method for configuring multiple collections with
         * default replication settings. If custom configurations are needed,
         * construct [MultipeerCollectionConfiguration] instances directly instead.
         *
         * @param collections A collection of [Collection] instances to replicate.
         * @return A set of [MultipeerCollectionConfiguration] instances for the provided collections.
         */
        public fun fromCollections(
            collections: kotlin.collections.Collection<Collection>
        ): Set<MultipeerCollectionConfiguration> {
            require(collections.isNotEmpty()) { "collections must not be empty" }
            return collections.map { MultipeerCollectionConfiguration(it) }.toSet()
        }
    }
}
