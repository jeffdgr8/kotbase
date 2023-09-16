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
 * DocumentFragment provides access to a document object. DocumentFragment
 * also provides subscript access by either key or index to the data
 * values of the document which are wrapped by Fragment objects.
 */
public class DocumentFragment
internal constructor(
    /**
     * Gets the document from the document fragment object.
     */
    public val document: Document? = null
) {

    /**
     * Checks whether the document exists in the database or not.
     */
    public val exists: Boolean
        get() = document != null

    /**
     * Subscript access to a Fragment object by key.
     *
     * @param key The key.
     */
    public operator fun get(key: String): Fragment {
        return if (document != null) {
            document[key]
        } else {
            Fragment()
        }
    }
}
