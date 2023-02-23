package com.couchbase.lite.kmp

import java.io.File

/**
 * CouchbaseLite Utility
 */
public object CouchbaseLite {

    /**
     * Initialize CouchbaseLite library. This method MUST be called before using CouchbaseLite.
     *
     * This method expects the current directory to be writeable
     * and will throw an `IllegalStateException` if it is not.
     * Use `init(boolean, File, File)` to specify alternative root and scratch directories.
     *
     * @param debug true if debugging
     * @throws IllegalStateException on initialization failure
     */
    @JvmOverloads
    public fun init(debug: Boolean = false) {
        com.couchbase.lite.CouchbaseLite.init(debug)
    }

    /**
     * Initialize CouchbaseLite library. This method MUST be called before using CouchbaseLite.
     *
     * This method allows specifying a default root directory for database files,
     * and the scratch directory used for temporary files (the native library, etc).
     * Both directories must be writable by this process.
     *
     * @param debug      true if debugging
     * @param rootDir    default directory for databases
     * @param scratchDir scratch directory for SQLite
     * @throws IllegalStateException on initialization failure
     */
    public fun init(debug: Boolean, rootDir: File, scratchDir: File) {
        com.couchbase.lite.CouchbaseLite.init(debug, rootDir, scratchDir)
    }
}
