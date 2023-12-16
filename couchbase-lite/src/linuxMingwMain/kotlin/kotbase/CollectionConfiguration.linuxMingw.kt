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

public actual class CollectionConfiguration {

    public actual constructor()

    public actual constructor(
        channels: List<String>?,
        documentIDs: List<String>?,
        pullFilter: ReplicationFilter?,
        pushFilter: ReplicationFilter?,
        conflictResolver: ConflictResolver?
    )

    internal constructor(config: CollectionConfiguration) : this(
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

    public actual var channels: List<String>? = null

    public actual var documentIDs: List<String>? = null

    public actual var conflictResolver: ConflictResolver? = null

    public actual var pullFilter: ReplicationFilter? = null

    public actual var pushFilter: ReplicationFilter? = null
}
