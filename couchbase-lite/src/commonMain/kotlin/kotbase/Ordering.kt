/*
 * Copyright 2022-2023 Jeff Lockhart
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

internal expect class OrderingPlatformState

/**
 * An Ordering represents a single ordering component in the query ORDER BY clause.
 */
public expect sealed class Ordering {

    internal val platformState: OrderingPlatformState

    /**
     * SortOrder represents a single ORDER BY entity. You can specify either ascending or
     * descending order. The default order is ascending.
     */
    public class SortOrder : Ordering {

        /**
         * Set the order as ascending order.
         *
         * @return the OrderBy object.
         */
        public fun ascending(): Ordering

        /**
         * Set the order as descending order.
         *
         * @return the OrderBy object.
         */
        public fun descending(): Ordering
    }

    public companion object {

        /**
         * Create a SortOrder, inherited from the OrderBy class, object by the given
         * property name.
         *
         * @param property the property name
         * @return the SortOrder object.
         */
        public fun property(property: String): SortOrder

        /**
         * Create a SortOrder, inherited from the OrderBy class, object by the given expression.
         *
         * @param expression the expression object.
         * @return the SortOrder object.
         */
        public fun expression(expression: Expression): SortOrder
    }
}
