@file:JvmName("ReplicationFilterJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase

import com.couchbase.lite.ReplicationFilter as CBLReplicationFilter

internal fun ReplicationFilter.convert(): CBLReplicationFilter =
    CBLReplicationFilter { document, flags ->
        invoke(Document(document), flags)
    }
