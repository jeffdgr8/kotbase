package kotbase

import com.couchbase.lite.internal.CouchbaseLiteInternal
import kotlinx.atomicfu.atomic
import java.io.File
import com.couchbase.lite.CouchbaseLite as CBLCouchbaseLite

public actual object CouchbaseLite {

    private val initCalled = atomic(false)

    /**
     * Initialize CouchbaseLite library. Unlike the Couchbase Lite Java SDK,
     * this method is optional to call before using CouchbaseLite. The no-parameter
     * `CouchbaseLite.init()` will be called automatically by default.
     *
     * This method expects the current directory to be writeable
     * and will throw an `IllegalStateException` if it is not.
     * Use `init(boolean, File, File)` to specify alternative root and scratch directories.
     *
     * @throws IllegalStateException on initialization failure
     */
    public fun init() {
        if (initCalled.getAndSet(true)) return
        resetInit()
        CBLCouchbaseLite.init()
    }

    /**
     * Initialize CouchbaseLite library. Unlike the Couchbase Lite Java SDK,
     * this method is optional to call before using CouchbaseLite. The no-parameter
     * `CouchbaseLite.init()` will be called automatically by default.
     *
     * This method expects the current directory to be writeable
     * and will throw an `IllegalStateException` if it is not.
     * Use `init(boolean, File, File)` to specify alternative root and scratch directories.
     *
     * @param debug true if debugging
     * @throws IllegalStateException on initialization failure
     */
    public fun init(debug: Boolean) {
        if (initCalled.getAndSet(true)) return
        resetInit()
        CBLCouchbaseLite.init(debug)
    }

    /**
     * Initialize CouchbaseLite library. Unlike the Couchbase Lite Java SDK,
     * this method is optional to call before using CouchbaseLite. The no-parameter
     * `CouchbaseLite.init()` will be called automatically by default.
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
        if (initCalled.getAndSet(true)) return
        resetInit()
        CBLCouchbaseLite.init(debug, rootDir, scratchDir)
    }

    /**
     * Allow default internalInit() to be overridden by manual init() call
     */
    private fun resetInit() {
        @Suppress("VisibleForTests")
        CouchbaseLiteInternal.reset(false)
    }

    /**
     * Default init that will guarantee to initialize native Couchbase Lite library
     * from [Database] and [DatabaseConfiguration] static initializers.
     * Doesn't set [initCalled] to allow a manual [init] call to succeed.
     */
    internal actual fun internalInit() {
        if (initCalled.value) return
        CBLCouchbaseLite.init()
    }
}
