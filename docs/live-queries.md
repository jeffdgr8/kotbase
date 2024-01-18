_Couchbase Lite database data querying concepts — live queries_

## Activating a Live Query

A live query is a query that, once activated, remains active and monitors the database for changes; refreshing the
result set whenever a change occurs. As such, it is a great way to build reactive user interfaces — especially
table/list views — that keep themselves up to date.

**So, a simple use case may be:** A replicator running and pulling new data from a server, whilst a live-query-driven UI
automatically updates to show the data without the user having to manually refresh. This helps your app feel quick and
responsive.

To activate a live query, just add a change listener to the query statement. It will be immediately active. When a
change is detected the query automatically runs, and posts the new query result to any observers (change listeners).

!!! example "<span id='example-1'>Example 1. Starting a Live Query</span>"

    ```kotlin
    val query = QueryBuilder
        .select(SelectResult.all())
        .from(DataSource.collection(collection)) 
    
    // Adds a query change listener.
    // Changes will be posted on the main queue.
    val token = query.addChangeListener { change ->
        change.results?.let { rs ->
            rs.forEach {
                println("results: ${it.keys}")
                /* Update UI */
            }
        } 
    }
    ```

1. Build the query statements.
2. Activate the live query by attaching a listener. Save the token in order to detach the listener and stop the query
   later — see [Example 2](#example-2).

!!! example "<span id='example-2'>Example 2. Stop a Live Query</span>"

    ```kotlin
    token.remove()
    ```

Here we use the change lister token from [Example 1](#example-1) to remove the listener. Doing so stops the live query.

## Using Kotlin Flows

Kotlin developers also have the option of using `Flow`s to feed query changes to the UI.

Define a live query as a `Flow` and activate a collector in the view creation function.

```kotlin
fun watchQuery(query: Query): Flow<List<Result>> {
    return query.queryChangeFlow()
        .mapNotNull { change ->
            val err = change.error
            if (err != null) {
               throw err
            }
            change.results?.allResults()
        }
}
```
