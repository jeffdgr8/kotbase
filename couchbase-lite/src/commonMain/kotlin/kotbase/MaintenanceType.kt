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

public expect enum class MaintenanceType {

    /**
     * Use only in collaboration with Couchbase Support.
     * (Runs SQLite `REINDEX`.)
     */
    REINDEX,

    /**
     * Shrinks the database file by removing any empty pages,
     * and deletes blobs that are no longer referenced by any documents.
     * (Runs SQLite `PRAGMA incremental_vacuum; PRAGMA wal_checkpoint(TRUNCATE)`.)
     */
    COMPACT,

    /**
     * Checks for database corruption, as might be caused by a damaged filesystem,
     * or memory corruption.
     * (Runs SQLite `PRAGMA integrity_check`.)
     */
    INTEGRITY_CHECK,

    /**
     * Quickly updates database statistics that may help optimize queries that have been run
     * by this Database since it was opened. The more queries that have been run, the more
     * effective this will be, but it tries to do its work quickly by scanning only portions
     * of indexes.
     * (Runs SQLite `PRAGMA analysis_limit=400; PRAGMA optimize`.)
     *
     * This operation is also performed automatically when a Database is closed.
     */
    OPTIMIZE,

    /**
     * Fully scans all indexes to gather database statistics that help optimize queries.
     * This may take some time, depending on the size of the indexes, but it doesn't have to
     * be redone unless the database changes drastically, or new indexes are created.
     * (Runs SQLite `PRAGMA analysis_limit=0; ANALYZE`.)
     */
    FULL_OPTIMIZE
}
