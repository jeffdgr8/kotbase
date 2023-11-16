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
 * Configuration for opening a database.
 */
public expect class DatabaseConfiguration {

    public constructor()

    /**
     * Copy constructor
     *
     * @param config the configuration to duplicate.
     */
    public constructor(config: DatabaseConfiguration?)

    /**
     * Set the canonical path of the directory in which to store the database.
     * If the directory doesn't already exist it will be created.
     * If it cannot be created an IllegalStateException will be thrown.
     *
     * @param directory the directory
     * @return this.
     * @throws IllegalStateException if the directory does not exist and cannot be created
     */
    public fun setDirectory(directory: String): DatabaseConfiguration

    /**
     * The path to the directory that contains the database.
     * If this path has not been set explicitly (see: `setDirectory` below),
     * then it is the system default.
     */
    public var directory: String
}
