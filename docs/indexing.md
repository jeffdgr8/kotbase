_Couchbase Lite database data model concepts - indexes_

## Introduction

Querying documents using a pre-existing database index is much faster because an index narrows down the set of documents
to examine — see the [Query Troubleshooting](query-troubleshooting.md) topic.

When planning the indexes you need for your database, remember that while indexes make queries faster, they may also:

* Make writes slightly slower, because each index must be updated whenever a document is updated
* Make your Couchbase Lite database slightly larger

Too many indexes may hurt performance. Optimal performance depends on designing and creating the _right_ indexes to go
along with your queries.

!!! note "Constraints"

    Couchbase Lite does not currently support partial value indexes; indexes with non-property expressions. You should
    only index with properties that you plan to use in the query.

## Creating a new index

You can use SQL++ or `QueryBuilder` syntaxes to create an index.

[Example 2](#example-2) creates a new index for the type and name properties, shown in this data model:

!!! example "Example 1. Data Model"

    ```json
    {
        "_id": "hotel123",
        "type": "hotel",
        "name": "The Michigander",
        "overview": "Ideally situated for exploration of the Motor City and the wider state of Michigan. Tripadvisor rated the hotel ...",
        "state": "Michigan"
    }
    ```

### SQL++

The code to create the index will look something like this:

!!! example "<span id='example-2'>Example 2. Create index</span>"

    ```kotlin
    collection.createIndex(
        "TypeNameIndex",
        ValueIndexConfiguration("type", "name")
    )
    ```

### QueryBuilder

!!! tip

    See the [QueryBuilder](query-builder.md) topic to learn more about `QueryBuilder`.

The code to create the index will look something like this:

!!! example "Example 3. Create index with QueryBuilder"

    ```kotlin
    collection.createIndex(
        "TypeNameIndex",
        IndexBuilder.valueIndex(
            ValueIndexItem.property("type"),
            ValueIndexItem.property("name")
        )
    )
    ```
