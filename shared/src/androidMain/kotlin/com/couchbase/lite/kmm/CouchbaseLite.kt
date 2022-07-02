package com.couchbase.lite.kmm

import android.content.Context
import com.couchbase.lite.internal.CouchbaseLiteInternal
import com.couchbase.lite.internal.core.C4
import com.couchbase.lite.internal.fleece.MValue
import com.couchbase.lite.internal.support.Log
import com.couchbase.lite.internal.utils.FileUtils
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Modifier

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
        val file = File(".")
        println(". path = ${file.absolutePath}")
        val field = CouchbaseLiteInternal::class.java
            .getDeclaredField("LITECORE_JNI_LIBRARY").apply {
                isAccessible = true
            }
        val modifiersField = Field::class.java.getDeclaredField("modifiers")
            .apply { isAccessible = true }
        modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())
        field.set(null, "LiteCore")
        try {
            com.couchbase.lite.CouchbaseLite.init(ctxt, debug)
        } catch (e: UnsatisfiedLinkError) {
            System.loadLibrary("LiteCore")

            C4.debug(debug)

            Log.initLogging(CouchbaseLiteInternal.loadErrorMessages(ctxt))

            val scratchDir = ctxt.getExternalFilesDir("CouchbaseLiteTemp")
            val setC4TmpDirPath = CouchbaseLiteInternal::class.java
                .getMethod("setC4TmpDirPath", File::class.java)
                .apply { isAccessible = true }
            setC4TmpDirPath.invoke(null, scratchDir)

            val mValueDelegate = Class.forName("com.couchbase.lite.MValueDelegate")
                .getConstructor().apply {
                    isAccessible = true
                }
            MValue.registerDelegate(mValueDelegate.newInstance() as MValue.Delegate)
        }
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
