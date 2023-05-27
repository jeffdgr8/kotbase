package com.udobny.kmp.couchbase.lite.ktx

import com.couchbase.lite.kmp.ResultSet

/**
 * Read count result from [selectCount].
 */
public inline fun ResultSet.countResult(): Long =
    allResults().first().getLong(0)
