package com.couchbase.lite.kmp.internal

import android.content.Context
import androidx.startup.Initializer
import com.couchbase.lite.kmp.CouchbaseLite

/**
 * Initializes CouchbaseLite library automatically on app startup
 * from androidx-startup Content Provider.
 */
public class CouchbaseLiteInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        CouchbaseLite.internalInit(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
