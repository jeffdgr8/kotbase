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

/**
 * SelectResult represents the result of a query.
 */
public expect open class SelectResult {

    /**
     * SelectResult.From is a SelectResult that you can specify the data source alias name.
     */
    public class From : SelectResult {

        /**
         * Specifies the data source alias for the SelectResult object.
         *
         * @param alias The data source alias name.
         * @return The SelectResult object with the data source alias name specified.
         */
        public infix fun from(alias: String): SelectResult
    }

    /**
     * SelectResult.As is a SelectResult with an alias.
     * The alias can be used as the key for accessing the result value from the query Result.
     */
    public class As : SelectResult {

        /**
         * Specifies the alias for the SelectResult object.
         *
         * @param alias The alias name.
         * @return The SelectResult object with the alias name specified.
         */
        public infix fun `as`(alias: String): As
    }

    public companion object {

        /**
         * Creates a SelectResult with the given property name.
         *
         * @param property The property name.
         * @return a SelectResult.From that can be used to alias the property.
         */
        public fun property(property: String): As

        /**
         * Creates a SelectResult object with the given expression.
         *
         * @param expression The expression.
         * @return a SelectResult.From that can be used to alias the property.
         */
        public fun expression(expression: Expression): As

        /**
         * Creates a SelectResult that contains values for all properties matching the query.
         * The result is a single CBLMutableDictionary whose key is the name of the data source.
         *
         * @return a SelectResult.From that can be used to alias the property.
         */
        public fun all(): From
    }
}
