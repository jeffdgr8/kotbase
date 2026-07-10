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

public actual class CollectionConfiguration
internal constructor(
    public actual val collection: Collection,
    public actual var channels: List<String>?,
    public actual var documentIDs: List<String>?,
    public actual var pullFilter: ReplicationFilter?,
    public actual var pushFilter: ReplicationFilter?,
    public actual var conflictResolver: ConflictResolver?
) {

    public actual constructor(collection: Collection) : this(collection, null, null, null, null, null)

    internal constructor(config: CollectionConfiguration) : this(
        config.collection,
        config.channels?.toList(),
        config.documentIDs?.toList(),
        config.pullFilter,
        config.pushFilter,
        config.conflictResolver
    )

    public actual fun setDocumentIDs(documentIDs: List<String>?): CollectionConfiguration {
        this.documentIDs = documentIDs
        return this
    }

    public actual fun setChannels(channels: List<String>?): CollectionConfiguration {
        this.channels = channels
        return this
    }

    public actual fun setConflictResolver(conflictResolver: ConflictResolver?): CollectionConfiguration {
        this.conflictResolver = conflictResolver
        return this
    }

    public actual fun setPullFilter(pullFilter: ReplicationFilter?): CollectionConfiguration {
        this.pullFilter = pullFilter
        return this
    }

    public actual fun setPushFilter(pushFilter: ReplicationFilter?): CollectionConfiguration {
        this.pushFilter = pushFilter
        return this
    }

    public actual companion object {

        public actual fun fromCollections(
            collections: kotlin.collections.Collection<Collection>
        ): Set<CollectionConfiguration> =
            collections.map { CollectionConfiguration(it) }.toSet()
    }
}
