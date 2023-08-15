_Couchbase Lite Database Sync - Synchronize changes between databases on the same device_

## Overview

!!! important "This is an [Enterprise Edition](https://www.couchbase.com/products/editions) feature."

Couchbase Lite supports replication between two local databases. This allows a Couchbase Lite replicator to store data
on secondary storage. It is especially useful in scenarios where a user’s device may be damaged and its data moved to a
different device.

!!! example "Example 1. Replication between Local Databases"

    ```kotlin
    val repl = Replicator(
        ReplicatorConfigurationFactory.create(
            database = database1,
            target = DatabaseEndpoint(database2),
            type = ReplicatorType.PULL
        )
    )

    // Start the replicator
    repl.start()
    // (be sure to hold a reference somewhere that will prevent it from being GCed)
    replicator = repl
    ```
