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

import android.content.Context
import com.couchbase.lite.BuildConfig
import com.couchbase.lite.internal.CouchbaseLiteInternal
import kotlinx.atomicfu.atomic
import java.io.File
import com.couchbase.lite.CouchbaseLite as CBLCouchbaseLite

/**
 * CouchbaseLite Utility
 */
public object CouchbaseLite {

    private val initCalled = atomic(false)

    /**
     * Initialize CouchbaseLite library. Unlike the Couchbase Lite Android SDK,
     * this method is optional to call before using CouchbaseLite. The single-parameter
     * `CouchbaseLite.init(Context)` will be called automatically by androidx-startup.
     *
     * @param ctxt the ApplicationContext.
     * @throws IllegalStateException on initialization failure
     */
    public fun init(ctxt: Context) {
        init(ctxt, BuildConfig.CBL_DEBUG)
    }

    /**
     * Initialize CouchbaseLite library. Unlike the Couchbase Lite Android SDK,
     * this method is optional to call before using CouchbaseLite. The single-parameter
     * `CouchbaseLite.init(Context)` will be called automatically by androidx-startup.
     *
     * @param debug true to enable debugging
     * @throws IllegalStateException on initialization failure
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
     * This method allows specifying a default root directory for database files,
     * and the scratch directory used for SQLite temporary files.
     * Use it with great caution.
     *
     * @param ctxt       Application context
     * @param debug      to enable debugging
     * @param rootDbDir  default directory for databases
     * @param scratchDir scratch directory for SQLite
     * @throws IllegalStateException on initialization failure
     */
    public fun init(ctxt: Context, debug: Boolean, rootDbDir: File, scratchDir: File) {
        if (initCalled.getAndSet(true)) return
        resetInit()
        CBLCouchbaseLite.init(ctxt, debug, rootDbDir, scratchDir)
    }

    /**
     * Allow default internalInit() to be overridden by manual init() call
     */
    private fun resetInit() {
        @Suppress("VisibleForTests")
        CouchbaseLiteInternal.reset(false)
    }

    /**
     * Default init that will auto initialize native Couchbase Lite library
     * from androidx-startup Content Provider on app startup.
     * Doesn't set [initCalled] to allow a manual [init] call to succeed.
     */
    internal fun internalInit(ctxt: Context) {
        if (initCalled.value) return
        init(ctxt)
        initCalled.value = false
    }
}

internal actual fun internalInit() {
    // no-op
    // Android default initialization is handled by androidx-startup
    // Content Provider in AndroidManifest.xml with internalInit(Context)
}
