@file:Suppress("NOTHING_TO_INLINE")

package com.udobny.kmp.couchbase.lite.ktx

import com.couchbase.lite.kmp.ResultSet

/**
 * Read count result from `selectCount()`.
 *
 * @see [selectCount]
 */
public inline fun ResultSet.countResult(): Long =
    allResults().first().getLong(0)
