_Couchbase Lite Database Sync — handling conflict between data changes_

## Causes of Conflicts

Document conflicts can occur if multiple changes are made to the same version of a document by multiple peers in a
distributed system. For Couchbase Mobile, this can be a Couchbase Lite or Sync Gateway database instance.

Such conflicts can occur after either of the following events:

* **A replication saves a document change** — in which case the change with the _most-revisions wins_ (unless one change
  is a delete). See [Conflicts when Replicating](#conflicts-when-replicating)
* **An application saves a document change directly to a database instance** — in which case, _last write wins_, unless
  one change is a delete — see [Conflicts when Updating](#conflicts-when-updating)

!!! note

    _Deletes_ **always win**. So, in either of the above cases, if one of the changes was a _delete_ then that change wins.

The following sections discuss each scenario in more detail.

!!! tip "Dive deeper …"

    Read more about [Document Conflicts and Automatic Conflict Resolution in Couchbase Mobile](
    https://blog.couchbase.com//document-conflicts-couchbase-mobile).

## Conflicts when Replicating

There’s no practical way to prevent a conflict when incompatible changes to a document are be made in multiple instances
of an app. The conflict is realized only when replication propagates the incompatible changes to each other.

!!! example "<span id='example-1'>Example 1. A typical replication conflict scenario</span>"

    1. Molly uses her device to create _DocumentA_.
    2. Replication syncs _DocumentA_ to Naomi’s device.
    3. Molly uses her device to apply _ChangeX_ to _DocumentA_.
    4. Naomi uses her device to make a different change, _ChangeY_, to _DocumentA_.
    5. Replication syncs _ChangeY_ to Molly’s device.<br><br>
       This device already has _ChangeX_ putting the local document in conflict.
    6. Replication syncs _ChangeX_ to Naomi’s device.<br><br>
       This device already has _ChangeY_ and now Naomi’s local document is in conflict.

### Automatic Conflict Resolution

!!! note

    The rules only apply to conflicts caused by replication. Conflict resolution takes place exclusively during pull
    replication, while push replication remains unaffected.

Couchbase Lite uses the following rules to handle conflicts such as those described in [A typical replication conflict
scenario](#example-1):

* If one of the changes is a deletion:<br><br>
  A deleted document (that is, a _tombstone_) always wins over a document update.
* If both changes are document changes:<br><br>
  The change with the most revisions will win.<br><br>
  Since each change creates a revision with an ID prefixed by an incremented version number, the winner is the change
  with the highest version number.

The result is saved internally by the Couchbase Lite replicator. Those rules describe the internal behavior of the
replicator. For additional control over the handling of conflicts, including when a replication is in progress, see
[Custom Conflict Resolution](#custom-conflict-resolution).

### Custom Conflict Resolution

Starting in Couchbase Lite 2.6, application developers who want more control over how document conflicts are handled can
use custom logic to select the winner between conflicting revisions of a document.

If a custom conflict resolver is not provided, the system will automatically resolve conflicts as discussed in
[Automatic Conflict Resolution](#automatic-conflict-resolution), and as a consequence there will be no conflicting
revisions in the database.

!!! warning "Caution"

    While this is true of any user defined functions, app developers must be strongly cautioned against writing
    sub-optimal custom conflict handlers that are time consuming and could slow down the client’s save operations.

To implement custom conflict resolution during replication, you must implement the following steps:

1. [Conflict Resolver](#conflict-resolver)
2. [Configure the Replicator](#configure-the-replicator)

#### Conflict Resolver

Apps have the following strategies for resolving conflicts:

* **Local Wins:** The current revision in the database wins.
* **Remote Wins:** The revision pulled from the remote endpoint through replication wins.
* **Merge:** Merge the content bodies of the conflicting revisions.

!!! example "Example 2. Using conflict resolvers"

    === "Local Wins"

        ```kotlin
        val localWinsResolver: ConflictResolver = { conflict ->
            conflict.localDocument
        }
        config.conflictResolver = localWinsResolver
        ```

    === "Remote Wins"

        ```kotlin
        val remoteWinsResolver: ConflictResolver = { conflict ->
            conflict.remoteDocument
        }
        config.conflictResolver = remoteWinsResolver
        ```

    === "Merge"

        ```kotlin
        val mergeConflictResolver: ConflictResolver = { conflict ->
            val localDoc = conflict.localDocument?.toMap()?.toMutableMap()
            val remoteDoc = conflict.remoteDocument?.toMap()?.toMutableMap()
        
            val merge: MutableMap<String, Any?>?
            if (localDoc == null) {
                merge = remoteDoc
            } else {
                merge = localDoc
                if (remoteDoc != null) {
                    merge.putAll(remoteDoc)
                }
            }
        
            if (merge == null) {
                MutableDocument(conflict.documentId)
            } else {
                MutableDocument(conflict.documentId, merge)
            }
        }
        config.conflictResolver = mergeConflictResolver
        ```

When a `null` document is returned by the resolver, the conflict will be resolved as a document deletion.

#### Important Guidelines and Best Practices

**Points of Note:**

* If you have multiple replicators, it is recommended that instead of distinct resolvers, you should use a unified
  conflict resolver across all replicators. Failure to do so could potentially lead to data loss under exception cases
  or if the app is terminated (by the user or an app crash) while there are pending conflicts.
* If the document ID of the document returned by the resolver does not correspond to the document that is in conflict
  then the replicator will log a warning message.

!!! important

    Developers are encouraged to review the warnings and fix the resolver to return a valid document ID.

* If a document from a different database is returned, the replicator will treat it as an error. A [document replication
  event](remote-sync-gateway.md#monitor-document-changes) will be posted with an error and an error message will be
  logged.

!!! important

    Apps are encouraged to observe such errors and take appropriate measures to fix the resolver function.

* When the replicator is stopped, the system will attempt to resolve outstanding and pending conflicts before stopping.
  Hence, apps should expect to see some delay when attempting to stop the replicator depending on the number of
  outstanding documents in the replication queue and the complexity of the resolver function.
* If there is an exception thrown in the [`ConflictResolver`](/api/couchbase-lite-ee/kotbase/-conflict-resolver/)
  function, the exception will be caught and handled:
    * The conflict to resolve will be skipped. The pending conflicted documents will be resolved when the replicator is
      restarted.
    * The exception will be reported in the warning logs.
    * The exception will be reported in the [document replication event](
      remote-sync-gateway.md#monitor-document-changes).

!!! important

    While the system will handle exceptions in the manner specified above, it is strongly encouraged for the resolver
    function to catch exceptions and handle them in a way appropriate to their needs.

#### Configure the Replicator

The implemented custom conflict resolver can be registered on the [`ReplicatorConfiguration`](
/api/couchbase-lite-ee/kotbase/-replicator-configuration/) object. The default value of the [`conflictResolver`](
/api/couchbase-lite-ee/kotbase/-replicator-configuration/conflict-resolver.html) is `null`. When the value is `null`,
the default conflict resolution will be applied.

!!! example "Example 3. A Conflict Resolver"

    ```kotlin
    val collectionConfig = CollectionConfiguration(conflictResolver = localWinsResolver)
    val repl = Replicator(
        ReplicatorConfiguration(URLEndpoint("ws://localhost:4984/mydatabase"))
            .addCollections(srcCollections, collectionConfig)
    )
    
    // Start the replicator
    // (be sure to hold a reference somewhere that will prevent it from being GCed)
    repl.start()
    this.replicator = repl
    ```

## Conflicts when Updating

When updating a document, you need to consider the possibility of update conflicts. Update conflicts can occur when you
try to update a document that’s been updated since you read it.

!!! example "Example 4. How Updating May Cause Conflicts"

    Here’s a typical sequence of events that would create an update conflict:

    1. Your code reads the document’s current properties, and constructs a modified copy to save.
    2. Another thread (perhaps the replicator) updates the document, creating a new revision with different properties.
    3. Your code updates the document with its modified properties, for example using
       [`Collection.save(MutableDocument)`](/api/couchbase-lite-ee/kotbase/-collection/save.html).

### Automatic Conflict Resolution

In Couchbase Lite, by default, the conflict is automatically resolved and only one document update is stored in the
database. The Last-Write-Win (LWW) algorithm is used to pick the winning update. So in effect, the changes from step 2
would be overwritten and lost.

If the probability of update conflicts is high in your app, and you wish to avoid the possibility of overwritten data,
the [`save()`](/api/couchbase-lite-ee/kotbase/-collection/save.html) and [`delete()`](
/api/couchbase-lite-ee/kotbase/-collection/delete.html) APIs provide additional method signatures with concurrency
control:

**Save operations**

[`Collection.save(MutableDocument, ConcurrencyControl)`](/api/couchbase-lite-ee/kotbase/-collection/save.html) —
attempts to save the document with a concurrency control.

The [`ConcurrencyControl`](/api/couchbase-lite-ee/kotbase/-concurrency-control/) parameter has two possible values:

* `LAST_WRITE_WINS` (default): The last operation wins if there is a conflict.
* `FAIL_ON_CONFLICT`: The operation will fail if there is a conflict.<br><br>
  In this case, the app can detect the error that is being thrown, and handle it by re-reading the document, making the
  necessary conflict resolution, then trying again.

**Delete operations**

As with save operations, delete operations also have two method signatures, which specify how to handle a possible
conflict:

* [`Collection.delete(Document)`](/api/couchbase-lite-ee/kotbase/-collection/delete.html): The last write will win if
  there is a conflict.
* [`Collection.delete(Document, ConcurrencyControl)`](/api/couchbase-lite-ee/kotbase/-collection/delete.html): attempts
  to delete the document with a concurrency control, with the same options described above.

### Custom Conflict Handlers

Developers can hook a conflict handler when saving a document, so they can easily handle the conflict in a single save
method call.

To implement custom conflict resolution when saving a document, apps must call the save method with a conflict handler
block ([`Collection.save(MutableDocument, ConflictHandler)`](/api/couchbase-lite-ee/kotbase/-collection/save.html)).

The following code snippet shows an example of merging properties from the existing document (`curDoc`) into the one
being saved (`newDoc`). In the event of conflicting keys, it will pick the key value from `newDoc`.

!!! example "Example 5. Merging document properties"

    ```kotlin
    val mutableDocument = collection.getDocument("xyz")?.toMutable() ?: return
    mutableDocument.setString("name", "apples")
    collection.save(mutableDocument) { newDoc, curDoc ->
        if (curDoc == null) {
            return@save false
        }
        val dataMap: MutableMap<String, Any?> = curDoc.toMap().toMutableMap()
        dataMap.putAll(newDoc.toMap())
        newDoc.setData(dataMap)
        true
    }
    ```
