package com.udobny.kmp.couchbase.lite.ktx

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.couchbase.lite.kmp.CouchbaseLite

actual fun initCouchbaseLite() {
    CouchbaseLite.init(getApplicationContext(), true)
}