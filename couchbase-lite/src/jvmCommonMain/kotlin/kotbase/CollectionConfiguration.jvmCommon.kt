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

import kotbase.internal.DelegatedClass
import com.couchbase.lite.CollectionConfiguration as CBLCollectionConfiguration

public actual class CollectionConfiguration
internal constructor(
    actual: CBLCollectionConfiguration,
    pullFilter: ReplicationFilter? = null,
    pushFilter: ReplicationFilter? = null,
    conflictResolver: ConflictResolver? = null
) : DelegatedClass<CBLCollectionConfiguration>(actual) {

    public actual constructor(
        channels: List<String>?,
        documentIDs: List<String>?,
        pullFilter: ReplicationFilter?,
        pushFilter: ReplicationFilter?,
        conflictResolver: ConflictResolver?
    ) : this(
        CBLCollectionConfiguration(
            channels,
            documentIDs,
            pullFilter?.convert(),
            pushFilter?.convert(),
            conflictResolver?.convert()
        ),
        pullFilter,
        pushFilter,
        conflictResolver
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

    public actual var channels: List<String>?
        get() = actual.channels
        set(value) {
            actual.setChannels(value)
        }

    public actual var documentIDs: List<String>?
        get() = actual.documentIDs
        set(value) {
            actual.setDocumentIDs(value)
        }

    public actual var conflictResolver: ConflictResolver? = conflictResolver
        set(value) {
            field = value
            actual.setConflictResolver(value?.convert())
        }

    public actual var pullFilter: ReplicationFilter? = pullFilter
        set(value) {
            field = value
            actual.setPullFilter(value?.convert())
        }

    public actual var pushFilter: ReplicationFilter? = pushFilter
        set(value) {
            field = value
            actual.setPushFilter(value?.convert())
        }
}
