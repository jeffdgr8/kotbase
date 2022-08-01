package com.couchbase.lite

import android.annotation.SuppressLint

@SuppressLint("VisibleForTests")
internal fun testReplicator(config: ReplicatorConfiguration): Replicator =
    Replicator(null, config)
