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
@file:Suppress("PropertyName")

package kotbase

@Suppress("UnusedReceiverParameter")
public val Defaults.VectorIndex: DefaultsVectorIndex
    get() = object : DefaultsVectorIndex {
        override val IS_LAZY: Boolean = false

        override val ENCODING: VectorEncoding.ScalarQuantizerType = VectorEncoding.ScalarQuantizerType.SQ8

        override val DISTANCE_METRIC: VectorIndexConfiguration.DistanceMetric =
            VectorIndexConfiguration.DistanceMetric.EUCLIDEAN_SQUARED

        override val MIN_TRAINING_SIZE: Long = 0

        override val MAX_TRAINING_SIZE: Long = 0

        override val NUM_PROBES: Long = 0
    }

public interface DefaultsVectorIndex {
    /**
     * Vectors are not lazily indexed, by default
     */
    public val IS_LAZY: Boolean

    /**
     * Vectors are encoded by using 8-bit Scalar Quantizer encoding, by default
     */
    public val ENCODING: VectorEncoding.ScalarQuantizerType

    /**
     * By default, vectors are compared using squared Euclidean metrics
     */
    public val DISTANCE_METRIC: VectorIndexConfiguration.DistanceMetric

    /**
     * By default, the value will be determined based on the number of centroids, encoding types, and the
     * encoding parameters.
     */
    public val MIN_TRAINING_SIZE: Long

    /**
     * By default, the value will be determined based on the number of centroids, encoding types, and the
     * encoding parameters
     */
    public val MAX_TRAINING_SIZE: Long

    /**
     * By default, the value will be determined based on the number of centroids.
     */
    public val NUM_PROBES: Long
}

@Suppress("UnusedReceiverParameter")
public val Defaults.Listener: DefaultsListener
    get() = object : DefaultsListener {
        override val PORT: Int = 0

        override val DISABLE_TLS: Boolean = false

        override val READ_ONLY: Boolean = false

        override val ENABLE_DELTA_SYNC: Boolean = false
    }

public interface DefaultsListener {
    /**
     * No port specified, the OS will assign one
     */
    public val PORT: Int

    /**
     * TLS is enabled on the connection
     */
    public val DISABLE_TLS: Boolean

    /**
     * The listener will allow database writes
     */
    public val READ_ONLY: Boolean

    /**
     * Delta sync is disabled for the listener
     */
    public val ENABLE_DELTA_SYNC: Boolean
}
