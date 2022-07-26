package com.couchbase.lite.kmm


@Suppress("NO_ACTUAL_FOR_EXPECT")
public expect open class IndexConfiguration {

    public val expressions: List<String>
}
