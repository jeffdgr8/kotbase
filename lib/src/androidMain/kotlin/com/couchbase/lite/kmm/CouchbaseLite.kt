package com.couchbase.lite.kmm

import android.content.Context
import java.io.File

public object CouchbaseLite {

    /**
     * Initialize CouchbaseLite library. This method MUST be called before using CouchbaseLite.
     */
    public fun init(ctxt: Context) {
        com.couchbase.lite.CouchbaseLite.init(ctxt)
    }

    /**
     * Initialize CouchbaseLite library. This method MUST be called before using CouchbaseLite.
     */
    public fun init(ctxt: Context, debug: Boolean) {
        com.couchbase.lite.CouchbaseLite.init(ctxt, debug)
    }

    /**
     * Initialize CouchbaseLite library.
     * This method allows specifying a root directory for CBL files.
     * Use this version with great caution.
     *
     * @param ctxt       Application context
     * @param debug      true if debugging
     * @param rootDbDir  default directory for databases
     * @param scratchDir scratch directory for SQLite
     */
    public fun init(ctxt: Context, debug: Boolean, rootDbDir: File, scratchDir: File) {
        com.couchbase.lite.CouchbaseLite.init(ctxt, debug, rootDbDir, scratchDir)
    }
}
