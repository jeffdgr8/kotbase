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

import cocoapods.CouchbaseLite.*
import kotlinx.cinterop.convert

public actual class VectorIndexConfiguration
private constructor(override val actual: CBLVectorIndexConfiguration) : IndexConfiguration(actual) {

    public actual constructor(expression: String, dimensions: Long, centroids: Long) : this(
        Unit.run {
            require(dimensions in 2..4096) { "Dimensions must be 2 <= dimensions <= 4096: $dimensions" }
            require(centroids in 1..64000) { "Centroids must be 1 <= centroids <= 64000: $centroids" }
            CBLVectorIndexConfiguration(expression, dimensions.convert(), centroids.convert())
        }
    )

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

    public actual val expression: String
        get() = actual.expression

    public actual val dimensions: Long
        get() = actual.dimensions.toLong()

    public actual val centroids: Long
        get() = actual.centroids.toLong()

    public actual var encoding: VectorEncoding = VectorEncoding.scalarQuantizer(Defaults.VectorIndex.ENCODING)
        set(value) {
            field = value
            actual.encoding = value.actual
        }

    public actual fun setEncoding(encoding: VectorEncoding): VectorIndexConfiguration {
        this.encoding = encoding
        return this
    }

    public actual var metric: DistanceMetric
        get() = DistanceMetric.from(actual.metric)
        set(value) {
            actual.metric = value.actual
        }

    public actual fun setMetric(metric: DistanceMetric): VectorIndexConfiguration {
        this.metric = metric
        return this
    }

    public actual var minTrainingSize: Long
        get() = actual.minTrainingSize.toLong()
        set(value) {
            actual.minTrainingSize = value.convert()
        }

    public actual fun setMinTrainingSize(minTrainingSize: Long): VectorIndexConfiguration {
        this.minTrainingSize = minTrainingSize
        return this
    }

    public actual var maxTrainingSize: Long
        get() = actual.maxTrainingSize.toLong()
        set(value) {
            actual.maxTrainingSize = value.convert()
        }

    public actual fun setMaxTrainingSize(maxTrainingSize: Long): VectorIndexConfiguration {
        this.maxTrainingSize = maxTrainingSize
        return this
    }

    public actual var numProbes: Long
        get() = actual.numProbes.toLong()
        set(value) {
            actual.numProbes = value.convert()
        }

    public actual fun setNumProbes(numProbes: Long): VectorIndexConfiguration {
        this.numProbes = numProbes
        return this
    }

    public actual var isLazy: Boolean
        get() = actual.isLazy
        set(value) {
            actual.isLazy = value
        }

    public actual fun setLazy(lazy: Boolean): VectorIndexConfiguration {
        this.isLazy = lazy
        return this
    }
}
