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
import kotlinx.coroutines.launch
import com.couchbase.lite.DocumentChangeListener as CBLDocumentChangeListener

internal fun DocumentChangeListener.convert(collection: Collection): CBLDocumentChangeListener =
    CBLDocumentChangeListener { change ->
        invoke(DocumentChange(change, collection))
    }

internal fun DocumentChangeSuspendListener.convert(collection: Collection, scope: CoroutineScope): CBLDocumentChangeListener =
    CBLDocumentChangeListener { change ->
        scope.launch {
            invoke(DocumentChange(change, collection))
        }
    }
