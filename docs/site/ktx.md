The KTX extensions include the excellent [Kotlin extensions by MOLO17](https://github.com/MOLO17/couchbase-lite-kotlin),
as well as other convenience functions for composing queries, observing change `Flow`s, and creating indexes.

## Installation

=== "Enterprise Edition"

    ```kotlin title="build.gradle.kts"
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("dev.kotbase:couchbase-lite-ee-ktx:{{ version_full }}")
            }
        }
    }
    ```

=== "Community Edition"

    ```kotlin title="build.gradle.kts"
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("dev.kotbase:couchbase-lite-ktx:{{ version_full }}")
            }
        }
    }
    ```

## Usage

### QueryBuilder extensions

The syntax for building a query is more straight-forward thanks to Kotlin's `infix` function support.

```kotlin
select(all()) from collection where { "type" equalTo "user" }
```

Or just a bunch of fields:

```kotlin
select("name", "surname") from collection where { "type" equalTo "user" }
```

Or if you also want the document ID:

```kotlin
select(Meta.id, all()) from collection where { "type" equalTo "user" }
select(Meta.id, "name", "surname") from collection where { "type" equalTo "user" }
```

You can even do more powerful querying:

```kotlin
select("name", "type")
    .from(collection)
    .where {
        (("type" equalTo "user") and ("name" equalTo "Damian")) or
        (("type" equalTo "pet") and ("name" like "Kitt"))
    }
    .orderBy { "name".ascending() }
    .limit(10)
```

There are also convenience extensions for performing `SELECT COUNT(*)` queries:

```kotlin
val query = selectCount() from collection where { "type" equalTo "user" }
val count = query.execute().countResult()
```

### Document builder DSL

For creating a `MutableDocument` ready to be saved, you can use a Kotlin builder DSL:

```kotlin
val document = MutableDocument {
    "name" to "Damian"
    "surname" to "Giusti"
    "age" to 24
    "pets" to listOf("Kitty", "Kitten", "Kitto")
    "type" to "user"
}

collection.save(document)
```

#### Collection creation functions

You can create a `MutableArray` or `MutableDictionary` using idiomatic `vararg` functions:

```kotlin
mutableArrayOf("hello", 42, true)
mutableDictOf("key1" to "value1", "key2" to 2, "key3" to null)
```

The similar `mutableDocOf` function allows nesting dictionary types, unlike the `MutableDocument` DSL:

```kotlin
mutableDocOf(
    "string" to "hello",
    "number" to 42,
    "array" to mutableArrayOf(1, 2, 3),
    "dict" to mutableDictOf("key" to "value")
)
```

### Flow support

Supplementing the `Flow` APIs from [Couchbase Lite Android KTX](
https://docs.couchbase.com/couchbase-lite/current/android/kotlin.html) present in the base **couchbase-lite** modules,
Kotbase KTX adds some additional useful `Flow` APIs.

#### Query Flow

`Query.asFlow()` builds on top of `Query.queryChangeFlow()` to emit non-null `ResultSet`s and throw any `QueryChange`
errors.

```kotlin
select(all())
    .from(collection)
    .where { "type" equalTo "user" }
    .asFlow()
    .collect { value: ResultSet -> 
        // consume ResultSet
    }
```

#### Document Flow

Unlike `Collection.documentChangeFlow()`, which only emits `DocumentChange`s, `Collection.documentFlow()` handles the common
use case of getting the initial document state and observing changes from the collection, enabling reactive UI patterns.

```kotlin
collection.documentFlow("userProfile")
    .collect { doc: Document? ->
        // consume Document
    }
```

### ResultSet model mapping

#### Map delegation

Thanks to [`Map` delegation](https://kotlinlang.org/docs/delegated-properties.html#delegating-to-another-property),
mapping a `ResultSet` to a Kotlin class has never been so easy.

The library provides the `ResultSet.toObjects()` and `Query.asObjectsFlow()` extensions for helping to map results given
a factory lambda.

Such factory lambdas accept a `Map<String, Any?>` and return an instance of a certain type. Those requirements fit
perfectly with a `Map`-delegated class.

```kotlin
class User(map: Map<String, Any?>) {
    val name: String by map
    val surname: String by map
    val age: Int by map
}

val users: List<User> = query.execute().toObjects(::User)

val usersFlow: Flow<List<User>> = query.asObjectsFlow(::User)
```

#### JSON deserialization

Kotbase KTX also provides extensions for mapping documents from a JSON string to Kotlin class. This works well
together with a serialization library, like [kotlinx-serialization](https://github.com/Kotlin/kotlinx.serialization), to
decode the JSON string to a Kotlin object.

```kotlin
@Serializable
class User(
    val name: String,
    val surname: String,
    val age: Int
)

val users: List<User> = query.execute().toObjects { json: String ->
    Json.decodeFromString<User>(json)
}

val usersFlow: Flow<List<User>> = query.asObjectsFlow { json: String ->
    Json.decodeFromString<User>(json)
}
```

### Index creation

Kotbase KTX provides concise top-level functions for index creation:

```kotlin
collection.createIndex("typeNameIndex", valueIndex("type", "name"))
collection.createIndex("overviewFTSIndex", fullTextIndex("overview"))
```

### Replicator extensions

For the Android platform, you can bind the `Replicator` `start()` and `stop()` methods to be performed automatically
when your [`Lifecycle`](https://developer.android.com/jetpack/androidx/releases/lifecycle)-enabled component gets
resumed or paused.

```kotlin
// Binds the Replicator to the Application lifecycle.
replicator.bindToLifecycle(ProcessLifecycleOwner.get().lifecycle)
```

```kotlin
// Binds the Replicator to the Activity/Fragment lifecycle.
// inside an Activity or Fragment...
override fun onCreate(savedInstanceState: Bundle?) {
    replicator.bindToLifecycle(lifecycle)
}
```

That's it! The `Replicator` will be automatically started when your component passes the `ON_RESUME` state, and it will
be stopped when the component passes the `ON_PAUSED` state. As you may imagine, no further action will be made after the
`ON_DESTROY` state.
