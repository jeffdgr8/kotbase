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

internal expect class SelectResultPlatformState

/**
 * SelectResult represents a single return value of the query statement.
 */
public expect open class SelectResult {

    internal val platformState: SelectResultPlatformState

    /**
     * SelectResult.From is a SelectResult that you can specify the data source alias name.
     */
    public class From : SelectResult {

        /**
         * Species the data source alias name to the SelectResult object.
         *
         * @param alias The data source alias name.
         * @return The SelectResult object with the data source alias name specified.
         */
        public fun from(alias: String): SelectResult
    }

    /**
     * SelectResult.As is a SelectResult that you can specify an alias name to it. The
     * alias name can be used as the key for accessing the result value from the query Result
     * object.
     */
    public class As : SelectResult {

        /**
         * Specifies the alias name to the SelectResult object.
         *
         * @param alias The alias name.
         * @return The SelectResult object with the alias name specified.
         */
        public fun `as`(alias: String): As
    }

    public companion object {

        /**
         * Creates a SelectResult object with the given property name.
         *
         * @param property The property name.
         * @return The SelectResult.As object that you can give the alias name to the returned value.
         */
        public fun property(property: String): As

        /**
         * Creates a SelectResult object with the given expression.
         *
         * @param expression The expression.
         * @return The SelectResult.As object that you can give the alias name to the returned value.
         */
        public fun expression(expression: Expression): As

        /**
         * Creates a SelectResult object that returns all properties data. The query returned result
         * will be grouped into a single CBLMutableDictionary object under the key of the data source name.
         *
         * @return The SelectResult.From object that you can specify the data source alias name.
         */
        public fun all(): From
    }
}
