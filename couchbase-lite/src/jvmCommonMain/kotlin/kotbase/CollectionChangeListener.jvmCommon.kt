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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.couchbase.lite.CollectionChangeListener as CBLCollectionChangeListener

internal fun CollectionChangeListener.convert(collection: Collection): CBLCollectionChangeListener =
    CBLCollectionChangeListener { change ->
        invoke(CollectionChange(change, collection))
    }

internal fun CollectionChangeSuspendListener.convert(
    collection: Collection,
    scope: CoroutineScope
): CBLCollectionChangeListener =
    CBLCollectionChangeListener { change ->
        scope.launch {
            invoke(CollectionChange(change, collection))
        }
    }
