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

import kotlinx.coroutines.CoroutineScope

internal sealed class DocumentChangeListenerHolder(
    val database: Database
)

internal class DocumentChangeDefaultListenerHolder(
    val listener: DocumentChangeListener,
    database: Database
) : DocumentChangeListenerHolder(database)

internal class DocumentChangeSuspendListenerHolder(
    val listener: DocumentChangeSuspendListener,
    database: Database,
    val scope: CoroutineScope
) : DocumentChangeListenerHolder(database)

internal sealed class CollectionDocumentChangeListenerHolder(
    val collection: Collection
)

internal class CollectionDocumentChangeDefaultListenerHolder(
    val listener: DocumentChangeListener,
    collection: Collection
) : CollectionDocumentChangeListenerHolder(collection)

internal class CollectionDocumentChangeSuspendListenerHolder(
    val listener: DocumentChangeSuspendListener,
    collection: Collection,
    val scope: CoroutineScope
) : CollectionDocumentChangeListenerHolder(collection)
