/*
 * Copyright 2024 Jeff Lockhart
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
 * Finds new or updated documents for which vectors need to be (re)computed and return an IndexUpdater
 * object used for setting the computed vectors for updating the index.The limit parameter is for
 * setting the max number of vectors to be computed. index.
 *
 * The limit parameter sets the maximum number of documents for which the index will be updated.
 *
 * If index is up-to-date, null will be returned.
 *
 * Note:
 *
 * The index updater is not guaranteed to find all unindexed documents at once. It may return fewer
 * than the limit, even if more documents exist. It is guaranteed to make progress by returning some
 * unindexed documents if there are any.  The intention is that the beginUpdate() will be periodically
 * called to update the index until null is returned, indicating that the index is up-to-date.
 *
 * @throws CouchbaseLiteException if the index is not lazy or not a vector index.
 */
@Throws(CouchbaseLiteException::class)
public expect fun QueryIndex.beginUpdate(limit: Int): IndexUpdater?
