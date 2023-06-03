package kotbase

import android.content.Context
import com.couchbase.lite.BuildConfig
import com.couchbase.lite.internal.CouchbaseLiteInternal
import kotlinx.atomicfu.atomic
import java.io.File

public actual object CouchbaseLite {

    private val initCalled = atomic(false)

    /**
     * Initialize CouchbaseLite library. Unlike the Couchbase Lite Android SDK,
     * this method is optional to call before using CouchbaseLite. The single-parameter
     * `CouchbaseLite.init(Context)` will be called automatically by androidx-startup.
     */
    public fun init(ctxt: Context) {
        init(ctxt, BuildConfig.CBL_DEBUG)
    }

    /**
     * Initialize CouchbaseLite library. Unlike the Couchbase Lite Android SDK,
     * this method is optional to call before using CouchbaseLite. The single-parameter
     * `CouchbaseLite.init(Context)` will be called automatically by androidx-startup.
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
        if (initCalled.getAndSet(true)) return
        resetInit()
        com.couchbase.lite.CouchbaseLite.init(ctxt, debug, rootDbDir, scratchDir)
    }

    /**
     * Allow default internalInit() to be overridden by manual init() call
     */
    private fun resetInit() {
        @Suppress("VisibleForTests")
        CouchbaseLiteInternal.reset(false)
    }

    internal actual fun internalInit() {
        // no-op
        // Android default initialization is handled by androidx-startup
        // Content Provider in AndroidManifest.xml with internalInit(Context)
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
