The paging extensions are built on Google's [AndroidX Paging](
https://developer.android.com/topic/libraries/architecture/paging/v3-overview). Kotbase Paging provides a
[`PagingSource`](https://developer.android.com/reference/kotlin/androidx/paging/PagingSource) which performs
limit/offset paging queries based on a user-supplied database query.

## Installation

=== "Enterprise Edition"

    ```kotlin title="build.gradle.kts"
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("dev.kotbase:couchbase-lite-ee-paging:{{ version_full }}")
            }
        }
    }
    ```

=== "Community Edition"

    ```kotlin title="build.gradle.kts"
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("dev.kotbase:couchbase-lite-paging:{{ version_full }}")
            }
        }
    }
    ```

## Usage

```kotlin
// Uses kotlinx-serialization JSON processor
@Serializable
data class Hotel(val id: String, val type: String, val name: String)

val select = select(Meta.id, "type", "name")
val mapper = { json: String ->
    Json.decodeFromString<Hotel>(json)
}
val queryProvider: From.() -> LimitRouter = {
    where {
        ("type" equalTo "hotel") and
        ("state" equalTo "California")
    }
    .orderBy { "name".ascending() }
}

val pagingSource = QueryPagingSource(
    EmptyCoroutineContext,
    select,
    collection,
    mapper,
    queryProvider
)
```
