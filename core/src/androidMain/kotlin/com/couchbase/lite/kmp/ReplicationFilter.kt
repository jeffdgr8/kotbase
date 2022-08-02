@file:JvmName("ReplicationFilterJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass
import java.util.EnumSet

internal fun ReplicationFilter.convert() = DelegatedReplicationFilter(this)

internal class DelegatedReplicationFilter
internal constructor(actual: ReplicationFilter) :
    DelegatedClass<ReplicationFilter>(actual),
    com.couchbase.lite.ReplicationFilter {

    override fun filtered(
        document: com.couchbase.lite.Document,
        flags: EnumSet<com.couchbase.lite.DocumentFlag>
    ): Boolean = actual(Document(document), flags)
}
