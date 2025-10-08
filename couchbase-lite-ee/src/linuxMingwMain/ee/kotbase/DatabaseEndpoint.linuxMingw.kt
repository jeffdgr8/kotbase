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

import cnames.structs.CBLEndpoint
import kotlinx.cinterop.CPointer
import libcblite.CBLEndpoint_CreateWithLocalDB

public actual class DatabaseEndpoint
internal constructor(
    actual: CPointer<CBLEndpoint>,
    public actual val database: Database
) : Endpoint(actual) {

    public actual constructor(database: Database) : this(
        CBLEndpoint_CreateWithLocalDB(database.actual)!!,
        database
    )

    override fun toString(): String =
        "DatabaseEndpoint{database=$database}"
}
