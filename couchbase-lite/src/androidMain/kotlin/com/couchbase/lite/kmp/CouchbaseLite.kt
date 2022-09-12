package com.couchbase.lite.kmp

import android.content.Context
import com.couchbase.lite.BuildConfig
import com.couchbase.lite.internal.CouchbaseLiteInternal
import java.io.File

public object CouchbaseLite {

    /**
     * Initialize CouchbaseLite library. This method MUST be called before using CouchbaseLite.
     */
    public fun init(ctxt: Context) {
        init(ctxt, BuildConfig.CBL_DEBUG)
    }

    /**
     * Initialize CouchbaseLite library. This method MUST be called before using CouchbaseLite.
     */
    public fun init(ctxt: Context, debug: Boolean) {
        init(
            ctxt,
            debug,
            ctxt.filesDir,
            // Handle nullable platform API
            // https://forums.couchbase.com/t/couchbaselite-init-illegalstateexception-tmp-dir-root-is-null/31651
            ctxt.getExternalFilesDir(CouchbaseLiteInternal.SCRATCH_DIR_NAME)
                ?: ctxt.getDir(CouchbaseLiteInternal.SCRATCH_DIR_NAME, Context.MODE_PRIVATE)
        )
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
