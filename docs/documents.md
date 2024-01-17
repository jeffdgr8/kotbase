_Couchbase Lite concepts — Data model — Documents_

## Overview

### Document Structure

In _Couchbase Lite_ the term 'document' refers to an entry in the database. You can compare it to a record, or a row in
a table.

Each document has an ID or unique identifier. This ID is similar to a primary key in other databases.

You can specify the ID programmatically. If you omit it, it will be automatically generated as a UUID.

!!! note

    Couchbase documents are assigned to a [Collection](databases.md#database-concepts). The ID of a document must be
    unique within the Collection it is written to. You cannot change it after you have written the document.

The document also has a value which contains the actual application data. This value is stored as a dictionary of
key-value (k-v) pairs. The values can be made of up several different [Data Types](#data-types) such as numbers,
strings, arrays, and nested objects.

### Data Encoding

The document body is stored in an internal, efficient, binary form called [Fleece](
https://github.com/couchbase/fleece#readme). This internal form can be easily converted into a manageable native
dictionary format for manipulation in applications.

Fleece data is stored in the smallest format that will hold the value whilst maintaining the integrity of the value.

### Data Types

The `Document` class offers a set of property accessors for various scalar types, such as:

* Boolean
* Date
* Double
* Float
* Int
* Long
* String

These accessors take care of converting to/from JSON encoding, and make sure you get the type you expect.

In addition to these basic data types Couchbase Lite provides for the following:

* **Dictionary** represents a read-only key-value pair collection
* **MutableDictionary** represents a writeable key-value pair collection
* **Array** represents a readonly ordered collection of objects
* **MutableArray** represents a writeable collection of objects
* **Blob** represents an arbitrary piece of binary data

### JSON

Couchbase Lite also provides for the direct handling of JSON data implemented in most cases by the provision of a
`toJSON()` method on appropriate API classes (for example, on `MutableDocument`, `Dictionary`, `Blob`, and `Array`) —
see [Working with JSON Data](#working-with-json-data).

## Constructing a Document

An individual document often represents a single instance of an object in application code.

You can consider a document as the equivalent of a 'row' in a relational table, with each of the document’s attributes
being equivalent to a 'column'.

Documents can contain nested structures. This allows developers to express many-to-many relationships without requiring
a reference or join table, and is naturally expressive of hierarchical data.

Most apps will work with one or more documents, persisting them to a local database and optionally syncing them, either
centrally or to the cloud.

In this section we provide an example of how you might create a `hotel` document, which provides basic contact details and
price data.

```title="Data Model"
hotel: {
  type: string (value = `hotel`)
  name: string
  address: dictionary {
    street: string
    city: string
    state: string
    country: string
    code: string
  }
  phones: array
  rate: float
}
```

### Open a Database

First open your database. If the database does not already exist, Couchbase Lite will create it for you.

Couchbase documents are assigned to a [Collection](databases.md#database-concepts). All the CRUD examples in this
document operate on a `collection` object.

```kotlin
// Get the database (and create it if it doesn’t exist).
val config = DatabaseConfiguration()
config.directory = "path/to/db"
val database = Database("getting-started", config)
val collection = database.getCollection("myCollection")
    ?: throw IllegalStateException("collection not found")
```

See [Databases](databases.md) for more information

### Create a Document

Now create a new document to hold your application’s data.

Use the mutable form, so that you can add data to the document.

```kotlin
// Create your new document
val mutableDoc = MutableDocument()
```

For more on using **Documents**, see [Document Initializers](#document-initializers) and [Mutability](#mutability).

### Create a Dictionary

Now create a mutable dictionary (`address`).

Each element of the dictionary value will be directly accessible via its own key.

```kotlin
// Create and populate mutable dictionary
// Create a new mutable dictionary and populate some keys/values
val address = MutableDictionary()
address.setString("street", "1 Main st.")
address.setString("city", "San Francisco")
address.setString("state", "CA")
address.setString("country", "USA")
address.setString("code", "90210")
```

!!! tip

    The Kotbase [KTX extensions](ktx.md#collection-creation-functions) provide an idiomatic `MutableDictionary` creation
    function:
    
    ```kotlin
    val address = mutableDictOf(
        "street" to "1 Main st.",
        "city" to "San Francisco",
        "state" to "CA",
        "country" to "USA",
        "code" to "90210"
    )
    ```

Learn more about [Using Dictionaries](#using-dictionaries).

### Create an Array

Since the hotel may have multiple contact numbers, provide a field (`phones`) as a mutable array.

```kotlin
// Create and populate mutable array
val phones = MutableArray()
phones.addString("650-000-0000")
phones.addString("650-000-0001")
```

!!! tip

    The Kotbase [KTX extensions](ktx.md#collection-creation-functions) provide an idiomatic `MutableArray` creation
    function:
    
    ```kotlin
    val phones = mutableArrayOf(
        "650-000-0000",
        "650-000-0001"
    )
    ```

Learn more about [Using Arrays](#using-arrays).

### Populate a Document

Now add your data to the mutable document created earlier. Each data item is stored as a key-value pair.

```kotlin
// Initialize and populate the document

// Add document type to document properties 
mutableDoc.setString("type", "hotel")

// Add hotel name string to document properties 
mutableDoc.setString("name", "Hotel Java Mo")

// Add float to document properties 
mutableDoc.setFloat("room_rate", 121.75f)

// Add dictionary to document's properties 
mutableDoc.setDictionary("address", address)

// Add array to document's properties 
mutableDoc.setArray("phones", phones)
```

!!! note

    Couchbase recommends using a type attribute to define each logical document type.

### Save a Document

Now persist the populated document to your Couchbase Lite database. This will auto-generate the document id.

```kotlin
// Save the document changes 
collection.save(mutableDoc)
```

### Close the Database

With your document saved, you can now close our Couchbase Lite database.

```kotlin
// Close the database 
database.close()
```

## Working with Data

### Checking a Document’s Properties

To check whether a given property exists in the document, use the [`Document.contains(key: String)`](
/api/couchbase-lite-ee/kotbase/-document/contains.html) method.

If you try to access a property which doesn’t exist in the document, the call will return the default value for that
getter method (0 for [`Document.getInt()`](/api/couchbase-lite-ee/kotbase/-document/get-int.html), 0.0 for
[`Document.getFloat()`](/api/couchbase-lite-ee/kotbase/-document/get-float.html), etc.).

### Date accessors

Couchbase Lite offers _Date_ accessors as a convenience. Dates are a common data type, but JSON doesn’t natively support
them, so the convention is to store them as strings in ISO-8601 format.

!!! example "Example 1. Date Getter"

    This example sets the date on the `createdAt` property and reads it back using the [`Document.getDate()`](
    /api/couchbase-lite-ee/kotbase/-document/get-date.html) accessor method.

    ```kotlin
    doc.setValue("createdAt", Clock.System.now())
    val date = doc.getDate("createdAt")
    ```

### Using Dictionaries

**API References**

* [Dictionary](/api/couchbase-lite-ee/kotbase/-dictionary/)
* [MutableDictionary](/api/couchbase-lite-ee/kotbase/-mutable-dictionary/)

!!! example "Example 2. Read Only"

    ```kotlin
    // NOTE: No error handling, for brevity (see getting started)
    val document = collection.getDocument("doc1")
    
    // Getting a dictionary from the document's properties
    val dict = document?.getDictionary("address")
    
    // Access a value with a key from the dictionary
    val street = dict?.getString("street")
    
    // Iterate dictionary
    dict?.forEach { key ->
        println("Key $key = ${dict.getValue(key)}")
    }
    
    // Create a mutable copy
    val mutableDict = dict?.toMutable()
    ```

!!! example "Example 3. Mutable"

    ```kotlin
    // NOTE: No error handling, for brevity (see getting started)
    
    // Create a new mutable dictionary and populate some keys/values
    val mutableDict = MutableDictionary()
    mutableDict.setString("street", "1 Main st.")
    mutableDict.setString("city", "San Francisco")
    
    // Add the dictionary to a document's properties and save the document
    val mutableDoc = MutableDocument("doc1")
    mutableDoc.setDictionary("address", mutableDict)
    collection.save(mutableDoc)
    ```

### Using Arrays

**API References**

* [Array](/api/couchbase-lite-ee/kotbase/-array/)
* [MutableArray](/api/couchbase-lite-ee/kotbase/-mutable-array/)

!!! example "Example 4. Read Only"

    ```kotlin
    // NOTE: No error handling, for brevity (see getting started)
    
    val document = collection.getDocument("doc1")
    
    // Getting a phones array from the document's properties
    val array = document?.getArray("phones")
    
    // Get element count
    val count = array?.count
    
    // Access an array element by index
    val phone = array?.getString(1)
    
    // Iterate array
    array?.forEachIndexed { index, item ->
        println("Row $index = $item")
    }
    
    // Create a mutable copy
    val mutableArray = array?.toMutable()
    ```

!!! example "Example 5. Mutable"

    ```kotlin
    // NOTE: No error handling, for brevity (see getting started)
    
    // Create a new mutable array and populate data into the array
    val mutableArray = MutableArray()
    mutableArray.addString("650-000-0000")
    mutableArray.addString("650-000-0001")
    
    // Set the array to document's properties and save the document
    val mutableDoc = MutableDocument("doc1")
    mutableDoc.setArray("phones", mutableArray)
    collection.save(mutableDoc)
    ```

### Using Blobs

For more on working with blobs, see [Blobs](blobs.md).

## Document Initializers

You can use the following methods/initializers:

* Use the [`MutableDocument()`](/api/couchbase-lite-ee/kotbase/-mutable-document/-mutable-document.html) initializer to
  create a new document where the document ID is randomly generated by the database.
* Use the [`MutableDocument(id: String?)`](/api/couchbase-lite-ee/kotbase/-mutable-document/-mutable-document.html)
  initializer to create a new document with a specific ID.
* Use the [`Collection.getDocument()`](/api/couchbase-lite-ee/kotbase/-collection/get-document.html) method to get a
  document. If the document doesn’t exist in the collection, the method will return `null`. You can use this behavior to
  check if a document with a given ID already exists in the collection.

!!! example "Example 6. Persist a document"

    ```kotlin
    val doc = MutableDocument()
    doc.apply {
        setString("type", "task")
        setString("owner", "todo")
        setDate("createdAt", Clock.System.now())
    }
    collection.save(doc)
    ```

!!! tip

    The Kotbase [KTX extensions](ktx.md#document-builder-dsl) provide a document builder DSL:
    
    ```kotlin
    val doc = MutableDocument {
        "type" to "task"
        "owner" to "todo"
        "createdAt" to Clock.System.now()
    }
    database.save(doc)
    ```

## Mutability

By default, a document is immutable when it is read from the database. Use [`Document.toMutable()`](
/api/couchbase-lite-ee/kotbase/-document/to-mutable.html) to create an updatable instance of the document.

!!! example "Example 7. Make a mutable document"

    Changes to the document are persisted to the database when the `save` method is called.

    ```kotlin
    collection.getDocument("xyz")?.toMutable()?.let {
        it.setString("name", "apples")
        collection.save(it)
    }
    ```

!!! note

    Any user change to the value of reserved keys (`_id`, `_rev`, or `_deleted`) will be detected when a document is
    saved and will result in an exception (Error Code 5 — `CorruptRevisionData`) — see also [Document
    Constraints](#document-constraints).

## Batch operations

If you’re making multiple changes to a database at once, it’s faster to group them together. The following example
persists a few documents in batch.

!!! example "Example 8. Batch operations"

    ```kotlin
    database.inBatch {
        for (i in 0..9) {
            val doc = MutableDocument()
            doc.apply {
                setValue("type", "user")
                setValue("name", "user $i")
                setBoolean("admin", false)
            }
            collection.save(doc)
            println("saved user document: ${doc.getString("name")}")
        }
    }
    ```

At the *local* level this operation is still transactional: no other `Database` instances, including ones managed by the
replicator, can make changes during the execution of the block, and other instances will not see partial changes. But
Couchbase Mobile is a distributed system, and due to the way replication works, there’s no guarantee that Sync Gateway
or other devices will receive your changes all at once.

## Document change events

You can register for document changes. The following example registers for changes to the document with ID `user.john`
and prints the `verified_account` property when a change is detected.

!!! example "Example 9. Document change events"

    ```kotlin
    collection.addDocumentChangeListener("user.john") { change ->
        collection.getDocument(change.documentID)?.let {
            println("Status: ${it.getString("verified_account")}")
        }
    }
    ```

### Using Kotlin Flows

Kotlin users can also take advantage of `Flow`s to monitor for changes.

The following methods show how to watch for document changes in a given collection or for changes to a specific document.

=== "Collection Changes"

    ```kotlin
    val collChanges: Flow<List<String>> = collection.collectionChangeFlow()
        .map { it.documentIDs }
    ```

=== "Document Changes"

    ```kotlin
    val docChanges: Flow<DocumentChange> = collection.documentChangeFlow("1001")
        .mapNotNull { change ->
            change.takeUnless {
                collection.getDocument(it.documentID)?.getString("owner").equals(owner)
            }
        }
    ```

## Document Expiration

Document expiration allows users to set the expiration date for a document. When the document expires, it is purged from
the database. The purge is not replicated to Sync Gateway.

!!! example "Example 10. Set document expiration"

    This example sets the TTL for a document to 1 day from the current time.

    ```kotlin
    // Purge the document one day from now
    collection.setDocumentExpiration(
        "doc123",
        Clock.System.now() + 1.days
    )
    
    // Reset expiration
    collection.setDocumentExpiration("doc1", null)
    
    // Query documents that will be expired in less than five minutes
    val query = QueryBuilder
        .select(SelectResult.expression(Meta.id))
        .from(DataSource.collection(collection))
        .where(
            Meta.expiration.lessThan(
                Expression.longValue((Clock.System.now() + 5.minutes).toEpochMilliseconds())
            )
        )
    ```

## Document Constraints

Couchbase Lite APIs do not explicitly disallow the use of attributes with the underscore prefix at the top level of
document. This is to facilitate the creation of documents for use either in _local only_ mode where documents are not
synced, or when used exclusively in peer-to-peer sync.

!!! note

    "_id", :"_rev" and "_sequence" are reserved keywords and must not be used as top-level attributes — see [Example 
    11](#example-11).

Users are cautioned that any attempt to sync such documents to Sync Gateway will result in an error. To be future-proof,
you are advised to avoid creating such documents. Use of these attributes for user-level data may result in undefined
system behavior.

For more guidance — see [Sync Gateway - data modeling guidelines](
https://docs.couchbase.com/sync-gateway/current/data-modeling.html)

!!! example "<span id='example-11'>Example 11. Reserved Keys List</span>"

    * _attachments
    * _deleted [^1]
    * _id [^1]
    * _removed
    * _rev [^1]
    * _sequence

[^1]: Any change to this reserved key will be detected when it is saved and will result in a Couchbase exception (Error
Code 5 — `CorruptRevisionData`)

## Working with JSON Data

**In this section**  
[Arrays](#arrays) | [Blobs](#blobs) | [Dictionaries](#dictionaries) | [Documents](#documents) | [Query Results as
JSON](#query-results-as-json)

The `toJSON()` typed-accessor means you can easily work with JSON data, native and Couchbase Lite objects.

### Arrays

Convert an `Array` to and from JSON using the [`toJSON()`](/api/couchbase-lite-ee/kotbase/-array/to-j-s-o-n.html) and
[`toList()`](/api/couchbase-lite-ee/kotbase/-array/to-list.html) methods — see [Example 12](#example-12).

Additionally, you can:

* Initialize a `MutableArray` using data supplied as a JSON string. This is done using the
  [`MutableArray(json: String)`](/api/couchbase-lite-ee/kotbase/-mutable-array/-mutable-array.html) constructor — see
  [Example 12](#example-12).
* Set data with a JSON string using [`setJSON()`](/api/couchbase-lite-ee/kotbase/-mutable-array/set-j-s-o-n.html).

!!! example "<span id='example-12'>Example 12. Arrays as JSON strings</span>"

    ```kotlin
    // JSON String -- an Array (3 elements. including embedded arrays)
    val jsonString = """[{"id":"1000","type":"hotel","name":"Hotel Ted","city":"Paris","country":"France","description":"Undefined description for Hotel Ted"},{"id":"1001","type":"hotel","name":"Hotel Fred","city":"London","country":"England","description":"Undefined description for Hotel Fred"},{"id":"1002","type":"hotel","name":"Hotel Ned","city":"Balmain","country":"Australia","description":"Undefined description for Hotel Ned","features":["Cable TV","Toaster","Microwave"]}]"""

    // initialize array from JSON string
    val mArray = MutableArray(jsonString)

    // Create and save new document using the array
    for (i in 0 ..< mArray.count) {
        mArray.getDictionary(i)?.apply {
            println(getString("name") ?: "unknown")
            collection.save(MutableDocument(getString("id"), toMap()))
        }
    }

    // Get an array from the document as a JSON string
    collection.getDocument("1002")?.getArray("features")?.apply {
        // Print its elements
        for (feature in toList()) {
            println("$feature")
        }
        println(toJSON())
    }
    ```

### Blobs

Convert a `Blob` to JSON using the [`toJSON()`](/api/couchbase-lite-ee/kotbase/-blob/to-j-s-o-n.html) method — see
[Example 13](#example-13).

You can use [`isBlob()`](/api/couchbase-lite-ee/kotbase/-blob/-companion/is-blob.html) to check whether a given
dictionary object is a blob or not — see [Example 13](#example-13).

Note that the blob object must first be saved to the database (generating the required metadata) before you can use the
[`toJSON()`](/api/couchbase-lite-ee/kotbase/-blob/to-j-s-o-n.html) method.

!!! example "<span id='example-13'>Example 13. Blobs as JSON strings</span>"

    ```kotlin
    val thisBlob = collection.getDocument("thisdoc-id")!!.toMap()
    if (!Blob.isBlob(thisBlob)) {
        return
    }
    val blobType = thisBlob["content_type"].toString()
    val blobLength = thisBlob["length"] as Number?
    ```

See also: [Blobs](blobs.md)

### Dictionaries

Convert a `Dictionary` to and from JSON using the [`toJSON()`](
/api/couchbase-lite-ee/kotbase/-dictionary/to-j-s-o-n.html) and [`toMap()`](
/api/couchbase-lite-ee/kotbase/-dictionary/to-map.html) methods — see [Example 14](#example-14).

Additionally, you can:

* Initialize a `MutableDictionary` using data supplied as a JSON string. This is done using the
  [`MutableDictionary(json: String)`](/api/couchbase-lite-ee/kotbase/-mutable-dictionary/-mutable-dictionary.html)
  constructor — see [Example 14](#example-14).
* Set data with a JSON string using [`setJSON()`](/api/couchbase-lite-ee/kotbase/-mutable-dictionary/set-j-s-o-n.html).

!!! example "<span id='example-14'>Example 14. Dictionaries as JSON strings</span>"

    ```kotlin
    val jsonString = """{"id":"1002","type":"hotel","name":"Hotel Ned","city":"Balmain","country":"Australia","description":"Undefined description for Hotel Ned","features":["Cable TV","Toaster","Microwave"]}"""

    val mDict = MutableDictionary(jsonString)
    println("$mDict")
    println("Details for: ${mDict.getString("name")}")
    mDict.forEach { key ->
        println(key + " => " + mDict.getValue(key))
    }
    ```

### Documents

Convert a `Document` to and from JSON strings using the [`toJSON()`](
/api/couchbase-lite-ee/kotbase/-document/to-j-s-o-n.html) and [`toMap()`](
/api/couchbase-lite-ee/kotbase/-document/to-map.html) methods — see [Example 15](#example-15).

Additionally, you can:

* Initialize a `MutableDocument` using data supplied as a JSON string. This is done using the
  [`MutableDocument(id: String?, json: String)`](
  /api/couchbase-lite-ee/kotbase/-mutable-document/-mutable-document.html) constructor — see [Example 15](#example-15).
* Set data with a JSON string using [`setJSON()`](/api/couchbase-lite-ee/kotbase/-mutable-document/set-j-s-o-n.html).

!!! example "<span id='example-15'>Example 15. Documents as JSON strings</span>"

    ```kotlin
    QueryBuilder
        .select(SelectResult.expression(Meta.id).`as`("metaId"))
        .from(DataSource.collection(srcColl))
        .execute()
        .forEach {
            it.getString("metaId")?.let { thisId ->
                srcColl.getDocument(thisId)?.toJSON()?.let { json ->
                    println("JSON String = $json")
                    val hotelFromJSON = MutableDocument(thisId, json)
                    dstColl.save(hotelFromJSON)
                    dstColl.getDocument(thisId)?.toMap()?.forEach { e ->
                        println("${e.key} => ${e.value}")
                    }
                }
            }
        }
    ```

### Query Results as JSON

Convert a query `Result` to a JSON string using its [`toJSON()`](
/api/couchbase-lite-ee/kotbase/-result/to-j-s-o-n.html) accessor method. The JSON string can easily be serialized or
used as required in your application. See [Example 16](#example-16) for a working example using [kotlinx-serialization](
https://github.com/Kotlin/kotlinx.serialization).

!!! example "<span id='example-16'>Example 16. Using JSON Results</span>"

    ```kotlin
    // Uses kotlinx-serialization JSON processor
    @Serializable
    data class Hotel(val id: String, val type: String, val name: String)

    val hotels = mutableListOf<Hotel>()

    val query = QueryBuilder
        .select(
            SelectResult.expression(Meta.id),
            SelectResult.property("type"),
            SelectResult.property("name")
        )
        .from(DataSource.collection(collection))

    query.execute().use { rs ->
        rs.forEach {

            // Get result as JSON string
            val json = it.toJSON()

            // Get JsonObject map from JSON string
            val mapFromJsonString = Json.decodeFromString<JsonObject>(json)

            // Use created JsonObject map
            val hotelId = mapFromJsonString["id"].toString()
            val hotelType = mapFromJsonString["type"].toString()
            val hotelName = mapFromJsonString["name"].toString()

            // Get custom object from JSON string
            val hotel = Json.decodeFromString<Hotel>(json)
            hotels.add(hotel)
        }
    }
    ```

#### JSON String Format

If your query selects ALL then the JSON format will be:

```
{
  database-name: {
    key1: "value1",
    keyx: "valuex"
  }
}
```

If your query selects a sub-set of available properties then the JSON format will be:

```
{
  key1: "value1",
  keyx: "valuex"
}
```
