package com.couchbase.lite

@Suppress("VisibleForTests")
internal fun testReplicator(config: ReplicatorConfiguration): Replicator =
    Replicator(null, config)
