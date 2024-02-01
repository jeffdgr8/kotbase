Scopes and collections allow you to organize your documents within a database.

!!! abstract "At a glance"

    **Use collections to organize your content in a database**
    
    For example, if your database contains travel information, airport documents can be assigned to an airports
    collection, hotel documents can be assigned to a hotels collection, and so on.
    
    * Document names must be unique within their collection.
    
    **Use scopes to group multiple collections**
    
    Collections can be assigned to different scopes according to content-type or deployment-phase (for example, test
    versus production).
    
    * Collection names must be unique within their scope.

## Default Scopes and Collections

Every database you create contains a default scope and a default collection named _default.

If you create a document in the database and don’t specify a specific scope or collection, it is saved in the default
collection, in the default scope.

If you upgrade from a version of Couchbase Lite prior to 3.1, all existing data is automatically placed in the default
scope and default collection.

The default scope and collection cannot be dropped.

## Create a Scope and Collection

In addition to the default scope and collection, you can create your own scope and collection when you create a
document.

Naming conventions for collections and scopes:

* Must be between 1 and 251 characters in length.
* Can only contain the characters `A-Z`, `a-z`, `0-9`, and the symbols `_`, `-`, and `%`.
* Cannot start with `_` or `%`.
* Scope names must be unique in databases.
* Collection names must be unique within a scope.

!!! note

    Scope and collection names are case sensitive.

!!! example "Example 1. Create a scope and collection"

    ```kotlin
    // create the collection "Verlaine" in the default scope ("_default")
    var collection1: Collection? = db.createCollection("Verlaine")
    // both of these retrieve collection1 created above
    collection1 = db.getCollection("Verlaine")
    collection1 = db.defaultScope.getCollection("Verlaine")
    
    // create the collection "Verlaine" in the scope "Television"
    var collection2: Collection? = db.createCollection("Television", "Verlaine")
    // both of these retrieve  collection2 created above
    collection2 = db.getCollection("Television", "Verlaine")
    collection2 = db.getScope("Television")!!.getCollection("Verlaine")
    ```

In the example above, you can see that `db.createCollection()` can take two parameters. The first is the scope assigned
to the created collection, if this parameter is omitted then a collection of the given name will be assigned to the
`_default` scope. In this case, creating a collection called `Verlaine`.

The second parameter is the name of the collection you want to create, in this case `Verlaine`. In the second section of
the example you can see `db.createCollection("Television", "Verlaine")`. This creates the collection `Verlaine` and then
checks to see if the scope `Television` exists. If the scope `Television` exists, the collection `Verlaine` is assigned
to the scope `Television`. If not, a new scope, `Television`, is created and then the collection `Verlaine` is assigned
to it.

!!! note

    You cannot create an empty user-defined scope. A scope is implicitly created in the `db.createCollection()` method.

## Index a Collection

!!! example "Example 2. Index a Collection"

    ```kotlin
    // Create an index named "nameIndex1" on the property "lastName" in the collection using the IndexBuilder
    collection.createIndex("nameIndex1", IndexBuilder.valueIndex(ValueIndexItem.property("lastName")))
    
    // Create a similar index named "nameIndex2" using an IndexConfiguration
    collection.createIndex("nameIndex2", ValueIndexConfiguration("lastName"))
    
    // get the names of all the indices in the collection
    val indices = collection.indexes
    
    // delete all the collection indices
    indices.forEach { collection.deleteIndex(it) }
    ```

## Drop a Collection

!!! example "Example 3. Drop a Collection"

    ```kotlin
    db.getCollection(collectionName, scopeName)?.let {
        db.deleteCollection(it.name, it.scope.name)
    }
    ```

!!! note

    There is no need to drop a user-defined scope. User-defined scopes are dropped when the collections associated with
    them contain no documents.

## List Scopes and Collections

!!! example "Example 4. List Scopes and Collections"

    ```kotlin
    // List all of the collections in each of the scopes in the database
    db.scopes.forEach { scope ->
        println("Scope :: ${scope.name}")
        scope.collections.forEach {
            println("    Collection :: ${it.name}")
        }
    }
    ```
