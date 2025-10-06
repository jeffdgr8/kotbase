/*
 * Copyright 2024 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kotbase

/**
 * Configuration for creating vector indexes.
 *
 * @constructor VectorIndexConfiguration Constructor.
 *
 * The number of centroids will be based on the expected number of vectors or documents containing the vectors
 * to be indexed and the application may need to make some experiments to see find an optimal value to use; one
 * simple rule which could be a starting point is to use the square root value of the number of vectors.
 *
 * @param expression The SQL++ expression returning a vector which is an array of numbers.
 * @param dimensions The number of dimensions of the vectors to be indexed.
 *                   The vectors that do not have the same dimensions  specified in the config will
 *                   not be indexed. The dimensions must be between 2 and 4096 inclusively.
 * @param centroids  The number of centroids which is the number buckets to partition the vectors in the index.
 *                   The number of centroids will be based on the expected number of vectors to be indexed;
 *                   one suggested rule is to use  the square root of the number of vectors. The centroids must
 *                   be between 1 and 64000 inclusively.
 */
public expect class VectorIndexConfiguration(
    expression: String,
    dimensions: Long,
    centroids: Long
) : IndexConfiguration {

    /**
     * Distance metric to be used in the vector indexes.
     */
    public enum class DistanceMetric {

        /**
         * Euclidean (L2) distance, squared
         */
        EUCLIDEAN_SQUARED,

        /**
         * Cosine distance (1.0 - cosine similarity)
         */
        COSINE,

        /**
         * Euclidean distance, _not_ squared, AKA L2
         */
        EUCLIDEAN,

        /**
         * Dot-product distance (negative of dot/inner product)
         */
        DOT;

        internal companion object
    }

    /**
     * The SQL++ expression returning a vector which is an array of 32-bits
     * floating-point numbers or a Base64 string representing an array of
     * 32-bits floating-point numbers in little-endian format.
     */
    public val expression: String

    /**
     * The number of vector dimensions.
     */
    public val dimensions: Long

    /**
     * The number of centroids which is the number buckets into which to partition the vectors in the index.
     */
    public val centroids: Long

    /**
     * Vector encoding type.
     *
     * The default value is an 8-bit Scalar Quantizer.
     */
    public var encoding: VectorEncoding

    /**
     * Vector encoding type.
     *
     * The default value is an 8-bit Scalar Quantizer.
     */
    public fun setEncoding(encoding: VectorEncoding): VectorIndexConfiguration

    /**
     * Distance Metric type
     *
     * The default value is euclidean.
     */
    public var metric: DistanceMetric

    /**
     * Distance Metric type
     *
     * The default value is euclidean.
     */
    public fun setMetric(metric: DistanceMetric): VectorIndexConfiguration

    /**
     * The minimum number of vectors for training the index. The default value
     * is zero, meaning that minTrainingSize will be automatically calculated by
     * the index based on the number of centroids specified, encoding types, and
     * the encoding parameters.
     *
     * Note:
     * The training will occur at or before the APPROX_VECTOR_DISANCE query is
     * executed, provided there is enough data at that time and, consequently, if
     * training is triggered during a query, the query may take longer to return
     * results.
     *
     * If a query is executed against the index before it is trained, a full
     * scan of the vectors will be performed. If there are insufficient vectors
     * in the database for training, a warning message will be logged,
     * indicating the required number of vectors.
     */
    public var minTrainingSize: Long

    /**
     * The minimum number of vectors for training the index. The default value
     * is zero, meaning that minTrainingSize will be automatically calculated by
     * the index based on the number of centroids specified, encoding types, and
     * the encoding parameters.
     *
     * Note:
     * The training will occur at or before the APPROX_VECTOR_DISANCE query is
     * executed, provided there is enough data at that time and, consequently, if
     * training is triggered during a query, the query may take longer to return
     * results.
     *
     * If a query is executed against the index before it is trained, a full
     * scan of the vectors will be performed. If there are insufficient vectors
     * in the database for training, a warning message will be logged,
     * indicating the required number of vectors.
     */
    public fun setMinTrainingSize(minTrainingSize: Long): VectorIndexConfiguration

    /**
     * The maximum number of vectors used for training the index. The default
     * value is zero, meaning that the maxTrainingSize will be automatically
     * calulated by the index based on the number of centroids specified,
     * encoding types, and the encoding parameters.
     */
    public var maxTrainingSize: Long

    /**
     * The maximum number of vectors used for training the index. The default
     * value is zero, meaning that the maxTrainingSize will be automatically
     * calulated by the index based on the number of centroids specified,
     * encoding types, and the encoding parameters.
     */
    public fun setMaxTrainingSize(maxTrainingSize: Long): VectorIndexConfiguration

    /**
     * The number of centroids that will be scanned during a query.
     * The default value is zero, meaning that the numProbes will be
     * automatically calulated by the index based on the number of centroids
     * specified
     */
    public var numProbes: Long

    /**
     * The number of centroids that will be scanned during a query.
     * The default value is zero, meaning that the numProbes will be
     * automatically calulated by the index based on the number of centroids
     * specified
     */
    public fun setNumProbes(numProbes: Long): VectorIndexConfiguration

    /**
     * The boolean flag indicating that index is lazy or not. The default value is false.
     *
     * If the index is lazy, the index will not automatically updated when the documents in the
     * collection are changed except when the documents are deleted or purged.
     *
     * When using the lazy index mode, the expression set in the config must refer to the values that will be
     * used later for computing vectors for the index instead of vector embeddings or prediction()
     * function that returns vectors.
     *
     * To update the index:
     *
     *  1. Use Collection's getIndex(name: String) to get the index.
     *  2. Call beginUpdate(int limit) on the index object with the max number of vectors to
     *     be updated into the index. The function will return an IndexUpdater object.
     *  3. For each of the `count` items, call a value getter such as getValue(at index: Int) on the
     *     IndexUpdater object to get the value for computing the vector. The returned value is
     *     based on the expression set to the VectorIndexConfiguration object used when
     *     creating the vector index.
     *  4. Call setVector() on the IndexUpdater object to set the computed vector or null to remove
     *     the index row.
     *  5. Call skipVector() on the IndexUpdater object to skip setting the vector. The value will be
     *     returned again next time when calling beginUpdate()  function on the index object.
     *  6. Call finish() on the IndexUpdater object to  save all the vector set to the IndexUpdater
     *     object.
     */
    public var isLazy: Boolean

    /**
     * The boolean flag indicating that index is lazy or not. The default value is false.
     *
     * If the index is lazy, the index will not automatically updated when the documents in the
     * collection are changed except when the documents are deleted or purged.
     *
     * When using the lazy index mode, the expression set in the config must refer to the values that will be
     * used later for computing vectors for the index instead of vector embeddings or prediction()
     * function that returns vectors.
     *
     * To update the index:
     *
     *  1. Use Collection's getIndex(name: String) to get the index.
     *  2. Call beginUpdate(int limit) on the index object with the max number of vectors to
     *     be updated into the index. The function will return an IndexUpdater object.
     *  3. For each of the `count` items, call a value getter such as getValue(at index: Int) on the
     *     IndexUpdater object to get the value for computing the vector. The returned value is
     *     based on the expression set to the VectorIndexConfiguration object used when
     *     creating the vector index.
     *  4. Call setVector() on the IndexUpdater object to set the computed vector or null to remove
     *     the index row.
     *  5. Call skipVector() on the IndexUpdater object to skip setting the vector. The value will be
     *     returned again next time when calling beginUpdate()  function on the index object.
     *  6. Call finish() on the IndexUpdater object to  save all the vector set to the IndexUpdater
     *     object.
     */
    public fun setLazy(lazy: Boolean): VectorIndexConfiguration
}
