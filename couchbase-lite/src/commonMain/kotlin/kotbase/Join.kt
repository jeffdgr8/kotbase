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

internal expect class JoinPlatformState

/**
 * A Join component representing a single JOIN clause in the query statement.
 */
public expect open class Join {

    internal val platformState: JoinPlatformState

    /**
     * Component used for specifying join on conditions.
     */
    public class On : Join {

        /**
         * Specify join conditions from the given expression.
         *
         * @param expression The Expression object specifying the join conditions.
         * @return The Join object that represents a single JOIN clause of the query.
         */
        public fun on(expression: Expression): Join
    }

    public companion object {

        /**
         * Create a JOIN (same as INNER JOIN) component with the given data source.
         * Use the returned On component to specify join conditions.
         *
         * @param datasource The DataSource object of the JOIN clause.
         * @return The On object used for specifying join conditions.
         */
        public fun join(datasource: DataSource): On

        /**
         * Create an INNER JOIN component with the given data source.
         * Use the returned On component to specify join conditions.
         *
         * @param datasource The DataSource object of the JOIN clause.
         * @return The On object used for specifying join conditions.
         */
        public fun innerJoin(datasource: DataSource): On

        /**
         * Create a LEFT JOIN (same as LEFT OUTER JOIN) component with the given data source.
         * Use the returned On component to specify join conditions.
         *
         * @param datasource The DataSource object of the JOIN clause.
         * @return The On object used for specifying join conditions.
         */
        public fun leftJoin(datasource: DataSource): On

        /**
         * Create a LEFT OUTER JOIN component with the given data source.
         * Use the returned On component to specify join conditions.
         *
         * @param datasource The DataSource object of the JOIN clause.
         * @return The On object used for specifying join conditions.
         */
        public fun leftOuterJoin(datasource: DataSource): On

        /**
         * Create an CROSS JOIN component with the given data source.
         * Use the returned On component to specify join conditions.
         *
         * @param datasource The DataSource object of the JOIN clause.
         * @return The Join object used for specifying join conditions.
         */
        public fun crossJoin(datasource: DataSource): Join
    }
}
