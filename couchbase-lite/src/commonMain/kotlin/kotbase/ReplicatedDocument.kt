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
 * Information about a Document updated by replication.
 */
public expect class ReplicatedDocument {

    /**
     * The scope of the collection to which the changed document belongs.
     */
    public val collectionScope: String

    /**
     * The name of the collection to which the changed document belongs.
     */
    public val collectionName: String

    /**
     * The document id of the changed document.
     */
    public val id: String

    /**
     * The current status flag of the document. e.g. deleted, access removed
     */
    public val flags: Set<DocumentFlag>

    /**
     * The current document replication error.
     */
    public val error: CouchbaseLiteException?
}
