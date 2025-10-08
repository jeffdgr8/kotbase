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

import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
import kotlinx.cinterop.convert
import libcblite.*
import platform.posix.strdup
import platform.posix.strlen

public actual class VectorIndexConfiguration
public actual constructor(
    public actual val expression: String,
    public actual val dimensions: Long,
    public actual val centroids: Long
) : IndexConfiguration(listOf(expression)) {

    init {
        require(dimensions in 2..4096) { "Dimensions must be 2 <= dimensions <= 4096: $dimensions" }
        require(centroids in 1..64000) { "Centroids must be 1 <= centroids <= 64000: $centroids" }
    }

    public actual enum class DistanceMetric {
        EUCLIDEAN_SQUARED,
        COSINE,
        EUCLIDEAN,
        DOT;

        internal val actual: CBLDistanceMetric
            get() = when (this) {
                EUCLIDEAN_SQUARED -> kCBLDistanceMetricEuclideanSquared
                COSINE -> kCBLDistanceMetricCosine
                EUCLIDEAN -> kCBLDistanceMetricEuclidean
                DOT -> kCBLDistanceMetricDot
            }

        internal actual companion object {

            internal fun from(distanceMetric: CBLDistanceMetric): DistanceMetric =
                when (distanceMetric) {
                    kCBLDistanceMetricEuclideanSquared -> EUCLIDEAN_SQUARED
                    kCBLDistanceMetricCosine -> COSINE
                    kCBLDistanceMetricEuclidean -> EUCLIDEAN
                    kCBLDistanceMetricDot -> DOT
                    else -> error("Unexpected CBLDistanceMetric ($distanceMetric)")
                }
        }
    }

    public actual var encoding: VectorEncoding = VectorEncoding.scalarQuantizer(Defaults.VectorIndex.ENCODING)

    public actual fun setEncoding(encoding: VectorEncoding): VectorIndexConfiguration {
        this.encoding = encoding
        return this
    }

    public actual var metric: DistanceMetric = Defaults.VectorIndex.DISTANCE_METRIC

    public actual fun setMetric(metric: DistanceMetric): VectorIndexConfiguration {
        this.metric = metric
        return this
    }

    public actual var minTrainingSize: Long = Defaults.VectorIndex.MIN_TRAINING_SIZE

    public actual fun setMinTrainingSize(minTrainingSize: Long): VectorIndexConfiguration {
        this.minTrainingSize = minTrainingSize
        return this
    }

    public actual var maxTrainingSize: Long = Defaults.VectorIndex.MAX_TRAINING_SIZE

    public actual fun setMaxTrainingSize(maxTrainingSize: Long): VectorIndexConfiguration {
        this.maxTrainingSize = maxTrainingSize
        return this
    }

    public actual var numProbes: Long = Defaults.VectorIndex.NUM_PROBES

    public actual fun setNumProbes(numProbes: Long): VectorIndexConfiguration {
        this.numProbes = numProbes
        return this
    }

    public actual var isLazy: Boolean = Defaults.VectorIndex.IS_LAZY

    public actual fun setLazy(lazy: Boolean): VectorIndexConfiguration {
        this.isLazy = lazy
        return this
    }

    internal val actual: CValue<CBLVectorIndexConfiguration>
        get() {
            val exp = expression
            return cValue {
                centroids = this@VectorIndexConfiguration.centroids.convert()
                dimensions = this@VectorIndexConfiguration.dimensions.convert()
                encoding = this@VectorIndexConfiguration.encoding.actual
                expression.buf = strdup(exp)
                expression.size = strlen(exp)
                expressionLanguage = kCBLN1QLLanguage
                isLazy = this@VectorIndexConfiguration.isLazy
                maxTrainingSize = this@VectorIndexConfiguration.maxTrainingSize.convert()
                metric = this@VectorIndexConfiguration.metric.actual
                minTrainingSize = this@VectorIndexConfiguration.minTrainingSize.convert()
                numProbes = this@VectorIndexConfiguration.numProbes.convert()
            }
        }
}
