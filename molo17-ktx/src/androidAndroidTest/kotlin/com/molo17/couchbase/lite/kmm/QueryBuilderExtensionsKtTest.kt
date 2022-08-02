package com.molo17.couchbase.lite.kmm

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.couchbase.lite.kmm.CouchbaseLite

actual fun initCouchbaseLite() {
    CouchbaseLite.init(getApplicationContext())
}
