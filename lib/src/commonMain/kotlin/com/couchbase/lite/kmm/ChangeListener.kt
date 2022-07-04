package com.couchbase.lite.kmm

public fun interface ChangeListener<T> {

    public fun changed(change: T)
}
