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
 * Meta is a factory class for creating the expressions that refer to
 * the metadata properties of the document.
 */
public expect object Meta {

    /**
     * A metadata expression referring to the ID of the document.
     */
    public val id: MetaExpression

    /**
     * A metadata expression referring to the RevisionId of the document.
     */
    public val revisionID: MetaExpression

    /**
     * A metadata expression referring to the sequence number of the document.
     * The sequence number indicates how recently the document has been changed. If one document's
     * `sequence` is greater than another's, that means it was changed more recently.
     */
    public val sequence: MetaExpression

    /**
     * A metadata expression referring to the deleted boolean flag of the document.
     */
    public val deleted: MetaExpression

    /**
     * A metadata expression referring to the expiration date of the document.
     */
    public val expiration: MetaExpression
}
