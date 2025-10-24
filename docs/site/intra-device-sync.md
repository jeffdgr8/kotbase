_Couchbase Lite Database Sync - Synchronize changes between databases on the same device_

## Overview

!!! important "This is an [Enterprise Edition](https://www.couchbase.com/products/editions/mobile/) feature."

Couchbase Lite supports replication between two local databases at the database, scope, or collection level. This allows
a Couchbase Lite replicator to store data on secondary storage. It is useful in scenarios when a user’s device is
damaged and its data is moved to a different device.

!!! example "Example 1. Replication between Local Databases"

    ```kotlin
    val repl = Replicator(
        ReplicatorConfiguration(DatabaseEndpoint(targetDb))
            .setType(ReplicatorType.PUSH)
            .addCollection(srcCollection)
    )

    // Start the replicator
    repl.start()
    // (be sure to hold a reference somewhere that will prevent it from being GCed)
    this.replicator = repl
    ```
