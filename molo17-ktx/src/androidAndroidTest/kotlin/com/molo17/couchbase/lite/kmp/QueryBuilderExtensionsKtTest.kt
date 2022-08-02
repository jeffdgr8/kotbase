package com.molo17.couchbase.lite.kmp

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.couchbase.lite.kmp.CouchbaseLite

actual fun initCouchbaseLite() {
    CouchbaseLite.init(getApplicationContext())
}
