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

    /**
     * Initializes a DatabaseConfiguration with default values.
     */
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
     * If it cannot be created a CouchbaseLiteError will be thrown.
     *
     * Note: The directory set by this method is the canonical path to the
     * directory whose path is passed. It is *NOT* necessarily the case that
     * `directory == config.setDirectory(directory).directory`
     *
     * @param directory the directory
     * @return this.
     * @throws CouchbaseLiteError if the directory does not exist and cannot be created
     */
    public fun setDirectory(directory: String): DatabaseConfiguration

    /**
     * The path to the directory that contains the database.
     *
     * If this path has not been set explicitly, then it is the system default.
     *
     * If the directory doesn't already exist it will be created.
     * If it cannot be created a CouchbaseLiteError will be thrown.
     *
     * Note: The directory set by this method is the canonical path to the
     * directory whose path is passed. It is *NOT* necessarily the case that
     * `directory == config.setDirectory(directory).directory`
     */
    public var directory: String

    /**
     * As Couchbase Lite normally configures its databases, there is a very small (though non-zero) chance that a
     * power failure at just the wrong time could cause the most recently committed transaction's changes to be lost.
     * This would cause the database to appear as it did immediately before that transaction. Setting this mode true
     * ensures that an operating system crash or power failure will not cause the loss of any data. Full sync mode is
     * very safe, but it is also **dramatically** slower.
     *
     * @param isFullSync true if full sync should be enabled
     * @return this
     */
    public fun setFullSync(fullSync: Boolean): DatabaseConfiguration

    /**
     * As Couchbase Lite normally configures its databases, there is a very small (though non-zero) chance that a
     * power failure at just the wrong time could cause the most recently committed transaction's changes to be lost.
     * This would cause the database to appear as it did immediately before that transaction. Setting this mode true
     * ensures that an operating system crash or power failure will not cause the loss of any data. Full sync mode is
     * very safe, but it is also **dramatically** slower.
     */
    public var isFullSync: Boolean

    /**
     * Advises Core to enable or disable memory-mapped Database files, if possible.
     * Memory-mapped database files are, currently, enabled by default, except on macOS
     * where they **cannot** be enabled at all.
     *
     * @param mmapEnabled true if memory-mapped database files should be enabled
     * @return this
     */
    public fun setMMapEnabled(mmapEnabled: Boolean): DatabaseConfiguration

    /**
     * Advises Core to enable or disable memory-mapped Database files, if possible.
     * Memory-mapped database files are, currently, enabled by default, except on macOS
     * where they **cannot** be enabled at all.
     */
    public var isMMapEnabled: Boolean
}
