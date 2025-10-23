_Use Vector Search with Full Text Search and Query._

## Use Vector Search

To configure a project to use vector search, follow the [installation instructions](installation.md#vector-search) to
add the Vector Search extension.

!!! note

    You must install Couchbase Lite to use the Vector Search extension.

## Create a Vector Index

This method shows how you can create a vector index using the Couchbase Lite Vector Search extension.

```kotlin
// create the configuration for a vector index named "vector"
// with 3 dimensions, 100 centroids, no encoding, using cosine distance
// with a max training size 5000 and amin training size 2500
// no vector encoding and using COSINE distance measurement
val config = VectorIndexConfiguration("vector", 3L, 100L).apply {
    encoding = VectorEncoding.none()
    metric = VectorIndexConfiguration.DistanceMetric.COSINE
    numProbes = 8L
    minTrainingSize = 2500L
    maxTrainingSize = 5000L
}
```

First, initialize the `config` object with the `VectorIndexConfiguration()` method with the following parameters:

* The expression of the data as a vector.
* The width or `dimensions` of the vector index is set to `3`.
* The amount of `centroids` is set to `100`. This means that there will be one hundred buckets with a single centroid
  each that gathers together similar vectors.

You can also alter some optional config settings such as `encoding`. From there, you create an index within a given
collection using the previously generated `config` object.

!!! note

    The number of vectors, the width or dimensions of the vectors and the training size can incur high CPU and memory
    costs as the size of each variable increases. This is because the training vectors have to be resident on the
    machine.

### Vector Index Configuration

The table below displays the different configurations you can modify within your `VectorIndexConfiguration()` function.
For more information on specific configurations, see [Vector Search](vector-search.md).

**Table 1. Vector Index Configuration Options**

| Configuration Name   |   Is Required    | Default Configuration                                                                                                                                              | Further Information                                                                                                                                                                                                                                                                                                  |
|:---------------------|:----------------:|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Expression           | :material-check: | No default                                                                                                                                                         | A SQL++ expression indicating where to get the vectors. A document property for embedded vectors or `prediction()` to call a registered Predictive model.                                                                                                                                                            |
| Number of Dimensions | :material-check: | No default                                                                                                                                                         | 2-4096                                                                                                                                                                                                                                                                                                               |
| Number of Centroids  | :material-check: | No default                                                                                                                                                         | 1-64000. The general guideline is an approximate square root of the number of documents.                                                                                                                                                                                                                             |
| Distance Metric      | :material-close: | Squared Euclidean Distance (euclideanSquared)                                                                                                                      | You can set the following alternates as your Distance Metric:<ul><li>cosine (1 - Cosine similarity)</li><li>Euclidean</li><li>dot (negated dot product)</li></ul>                                                                                                                                                    |
| Encoding             | :material-close: | Scalar Quantizer(SQ) or SQ-8 bits                                                                                                                                  | There are three possible configurations:<ul><li>None No compression, No data loss</li><li>Scalar Quantizer (SQ) or SQ-8 bits (Default) Reduces the number of bits per dimension</li><li>Product Quantizer (PQ) Reduces the number of dimensions and bits per dimension</li></ul>                                     |
| Training Size        | :material-close: | The default values for both the minimum and maximum training size is zero. The training size is calculated based on the number of Centroids and the encoding type. | The guidelines for the minimum and maximum training size are as follows:<ul><li>The minimum training size is set to 25x the number of Centroids or 2<sup>PQ’s bits</sup> when PQ is used</li><li>The maximum training size is set to 256x the number of Centroids or 2<sup>PQ’s bits</sup> when PQ is used</li></ul> |
| NumProbes            | :material-close: | The default value is 0. The number of Probes is calculated based on the number of Centroids.                                                                       | A guideline for setting a custom number of probes is at least 8 or 0.5% the number of Centroids.                                                                                                                                                                                                                     |
| isLazy               | :material-close: | False                                                                                                                                                              | Setting the value to true will enable lazy mode for the vector index.                                                                                                                                                                                                                                                |

!!! warning "Caution"

    Altering the default training sizes could be detrimental to the accuracy of returned results produced by the model
    and total computation time.

## Generating Vectors

You can use the following methods to generate vectors in Couchbase Lite:

1. You can call a Machine Learning(ML) model, and embed the generated vectors inside the documents.
2. You can use the `prediction()` function to generate vectors to be indexed for each document at the indexing time.
3. You can use Lazy Vector Index (lazy index) to generate vectors asynchronously from remote ML models that may not always be reachable or functioning, skipping or scheduling retries for those specific cases.

Below are example configurations of the previously mentioned methods.

### Create a Vector Index with Embeddings

This method shows you how to create a Vector Index with embeddings.

```kotlin
// Get the collection named "colors" in the default scope.
val collection = database.getCollection("colors")
    ?: throw IllegalStateException("No such collection: colors")

// Create a vector index configuration with a document property named "vector",
// 3 dimensions, and 100 centroids.
val config = VectorIndexConfiguration("vector", 3, 100)
// Create a vector index from the configuration with the name "colors_index".
collection.createIndex("colors_index", config)
```

1. First, create the standard configuration, setting up an expression, number of dimensions and number of centroids for
   the vector embedding.
2. Next, create a vector index, `colors_index`, on a collection and pass it the configuration.

### Create Vector Index Embeddings from a Predictive Model

This method generates vectors to be indexed for each document at the index time by using the `prediction()` function.
The key difference to note is that the `config` object uses the output of the `prediction()` function as the
`expression` parameter to generate the vector index.

```kotlin
class ColorModel : PredictiveModel {
    override fun predict(input: Dictionary): Dictionary? {
        // Get the color input from the input dictionary
        val color = input.getString("colorInput")
            ?: throw IllegalStateException("No input color found")

        // Use ML model to get a vector (an array of floats) for the input color.
        val vector = Color.getVector(color) ?: return null

        // Create an output dictionary by setting the vector result to
        // the dictionary key named "vector".
        val output = MutableDictionary()
        output.setValue("vector", vector)
        return output
    }
}

fun createVectorIndexFromPredictiveIndex() {
    // Register the predictive model named "ColorModel".
    Database.prediction.registerModel("ColorModel", ColorModel())

    // Create a vector index configuration with an expression using the prediction
    // function to get the vectors from the registered predictive model.
    val expression = "prediction(ColorModel, {\"colorInput\": color}).vector"
    val config = VectorIndexConfiguration(expression, 3, 100)

    // Create vector index from the configuration
    collection.createIndex("colors_index", config)
}
```

!!! note

    You can use less storage by using the `prediction()` function as the encoded vectors will only be stored in the
    index. However, the index time will be longer as vector embedding generation is occurring at run time.

## Create a Lazy Vector Index

Lazy indexing is an alternate approach to using the standard predictive model with regular vector indexes which handle
the indexing process automatically. You can use lazy indexing to use a ML model that is not available locally on the
device and to create vector indexes without having vector embeddings in the documents.

```kotlin
// Creating a lazy vector index using the document's property named "color".
// The "color" property's value will be used to compute a vector when updating the index.
val config = VectorIndexConfiguration("color", 3, 100).apply { 
    isLazy = true
}
```

You can enable lazy vector indexing by setting the `isLazy` property to `true` in your vector index configuration.

!!! note

    Lazy Vector Indexing is opt-in functionality, the `isLazy` property is set to `false` by default.

### Updating the Lazy Index

Below is an example of how you can update your lazy index.

```kotlin
val index = collection.getIndex("colors_index")
    ?: throw IllegalStateException("colors_index not found")

while (true) {
    // Start an update on it (in this case, limit to 50 entries at a time)
    index.beginUpdate(50)?.use { updater ->
        for (i in 0..<updater.count) {
            // The value type will depend on the expression you have set in your index.
            // In this example, it is a string property.
            val color = updater.getString(i)

            try {
                val embedding: List<Float>? = Color.getVectorAsync(color)
                // Set the computed vector here. If vector is null, calling setVector
                // will cause the underlying document to NOT be indexed.
                updater.setVector(embedding, i)
            } catch (e: IOException) {
                // Bad connection? Corrupted over the wire? Something bad happened
                // and the vector cannot be generated at the moment: skip it.
                // The next time beginUpdate() is called, we'll try it again.
                updater.skipVector(i)
            }
        }
        // This writes the vectors to the index. You MUST either have set or skipped each
        // of the vectors in the updater or this call will throw an exception.
        updater.finish()
    }
    // loop until there are no more vectors to update
        ?: break
}
```

You procedurally update the vectors in the index by looping through the vectors in batches until you reach the value of
the `limit` parameter.

The update process follows the following sequence:

1. Get a value for the updater.
    1. If the there is no value for the vector, handle it. In this case, the vector will be skipped and considered the
       next time `beginUpdate()` is called.
    
    !!! note
    
        A key benefit of lazy indexing is that the indexing process continues if a vector fails to generate. For
        standard vector indexing, this will cause the affected documents to be dropped from the indexing process.

2. Set the vector from the computed vector derived from the updater value and your ML model.
    1. If there is no value for the vector, this will result in the underlying document to not be indexed.
3. Once all vectors have completed the update loop, finish updating.

!!! note

    `updater.finish()` will throw an error if any values inside the updater have not been set or skipped.

## Vector Search SQL++ Support

Couchbase Lite currently supports Hybrid Vector Search and the `APPROX_VECTOR_DISTANCE()` function.

!!! important

    Similar to the [Full Text Search](full-text-search.md) `match()` function, the `APPROX_VECTOR_DISTANCE()` function
    and Hybrid Vector Search cannot use the `OR` expression with the other expressions in the related `WHERE` clause.

## Use Hybrid Vector Search

You can use Hybrid Vector Search (Hybrid Search) to perform vector search in conjunction with regular SQL++ queries.
With Hybrid Search, you perform vector search on documents that have already been filtered based on criteria specified
in the `WHERE` clause.

!!! note

    A `LIMIT` clause is required for non-hybrid Vector Search, this avoids a slow, exhaustive unlimited search of all
    possible vectors.

### Hybrid Vector Search with Full Text Match

Below are examples of using Hybrid Search with the Full Text `match()` function.

```kotlin
// Create a hybrid vector search query with full-text's match() that
// uses the the full-text index named "color_desc_index".
val sql = $$"""
    SELECT meta().id, color
    WHERE MATCH(color_desc_index, $text)
    ORDER BY approx_vector_distance(vector, $vector)
    LIMIT 8
""".trimIndent()

val query = database.createQuery(sql)

// Get a vector, an array of float numbers, for the input color code (e.g. FF000AA).
// Normally, you will get the vector from your ML model.
val vector = Color.getVector("FF00AA")
    ?: throw IllegalStateException("Vector not found")

val parameters = Parameters()
// Set the vector array to the parameter "$vector"
parameters.setValue("vector", vector)
// Set the vector array to the parameter "$text".
parameters.setString("text", "vibrant")
query.parameters = parameters

// Execute the query
query.execute().use { rs ->
    // process results
}
```

### Prediction with Hybrid Vector Search

Below are examples of using Hybrid Search with an array of vectors generated by the `Prediction()` function at index
time.

```kotlin
// Create a hybrid vector search query that uses prediction() for computing vectors.
val sql = $$"""
    SELECT meta().id, color
    WHERE saturation > 0.5
    ORDER BY approx_vector_distance(prediction(ColorModel, {"colorInput": color}).vector, $vector)
    LIMIT 8
""".trimIndent()

val query = database.createQuery(sql)

// Get a vector, an array of float numbers, for the input color code (e.g. FF000AA).
// Normally, you will get the vector from your ML model.
val vector = Color.getVector("FF00AA")
    ?: throw IllegalStateException("Vector not found")

// Set the vector array to the parameter "$vector"
val parameters = Parameters()
parameters.setValue("vector", vector)
query.parameters = parameters

// Execute the query
query.execute().use { rs ->
    // process results
}
```

## `APPROX_VECTOR_DISTANCE(vector-expr, target-vector, [metric], [nprobes], [accurate])`

!!! warning

    If you use a different distance metric in the `APPROX_VECTOR_DISTANCE()` function from the one configured in the
    index, you will receive an error when compiling the query.

| Parameter     |   Is Required    | Description                                                                                                                                                                                                                                                                                                                |
|:--------------|:----------------:|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| vector-expr   | :material-check: | The expression returning a vector (NOT Index Name). Must match the expression specified in the vector index exactly.                                                                                                                                                                                                       |
| target-vector | :material-check: | The target vector.                                                                                                                                                                                                                                                                                                         |
| metric        | :material-close: | Values : "EUCLIDEAN_SQUARED", “L2_SQUARED”, “EUCLIDEAN”, “L2”, ”COSINE”, “DOT”. If not specified, the metric set in the vector index is used. If specified, the metric must match with the metric set in the vector index. This optional parameter allows multiple indexes to be attached to the same field in a document. |
| nprobes       | :material-close: | Number of buckets to search for the nearby vectors. If not specified, the nprobes set in the vector index is used.                                                                                                                                                                                                         |
| accurate      | :material-close: | If not present, false will be used, which means that the quantized/encoded vectors in the index will be used for calculating the distance.<br><br>IMPORTANT: Only accurate = false is supported                                                                                                                            |

### Use `APPROX_VECTOR_DISTANCE()`

```kotlin
// Create a vector search query by using the approx_vector_distance() in WHERE clause.
val sql = $$"""
    SELECT meta().id, color
    FROM _default.colors
    WHERE approx_vector_distance(vector, $vector) < 0.5
    LIMIT 8
""".trimIndent()

val query = database.createQuery(sql)

// Get a vector, an array of float numbers, for the input color code (e.g. FF000AA).
// Normally, you will get the vector from your ML model.
val vector = Color.getVector("FF00AA")
    ?: throw IllegalStateException("Vector not found")

// Set the vector array to the parameter "$vector"
val parameters = Parameters()
parameters.setValue("vector", vector)
query.parameters = parameters

// Execute the query
query.execute().use { rs ->
    // process results
}
```

This function returns the approximate distance between a given vector, typically generated from your ML model, and an
array of vectors with size equal to the `LIMIT` parameter, collected by a SQL++ query using `APPROX_VECTOR_DISTANCE()`.

### Prediction with `APPROX_VECTOR_DISTANCE()`

Below are examples of using `APPROX_VECTOR_DISTANCE()` with an array of vectors generated by the `Prediction()` function
at index time.

```kotlin
// Create a vector search query that uses prediction() for computing vectors.
val sql = $$"""
    SELECT meta().id, color
    FROM _default.colors
    ORDER BY APPROX_VECTOR_DISTANCE(prediction(ColorModel, {"colorInput": color}).vector, $vector)
    LIMIT 8
""".trimIndent()

val query = database.createQuery(sql)

// Get a vector, an array of float numbers, for the input color code (e.g. FF000AA).
// Normally, you will get the vector from your ML model.
val vector = Color.getVector("FF00AA")
    ?: throw IllegalStateException("Vector not found")

// Set the vector array to the parameter "$vector"
val parameters = Parameters()
parameters.setValue("vector", vector)
query.parameters = parameters

// Execute the query
query.execute().use { rs ->
    // process results
}
```
