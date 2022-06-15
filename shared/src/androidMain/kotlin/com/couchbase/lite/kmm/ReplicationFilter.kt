package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass
import java.util.EnumSet

internal fun ReplicationFilter.convert() = DelegatedReplicationFilter(this)

internal class DelegatedReplicationFilter
internal constructor(actual: ReplicationFilter) :
    DelegatedClass<ReplicationFilter>(actual),
    com.couchbase.lite.ReplicationFilter {

    override fun filtered(
        document: com.couchbase.lite.Document,
        flags: EnumSet<com.couchbase.lite.DocumentFlag>
    ): Boolean = actual.filtered(Document(document), flags)
}
