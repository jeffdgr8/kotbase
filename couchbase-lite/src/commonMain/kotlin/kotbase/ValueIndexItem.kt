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
 * Value Index Item
 */
public expect class ValueIndexItem {

    public companion object {

        /**
         * Creates a value index item with the given property.
         *
         * @param property the property name
         * @return The value index item
         */
        public fun property(property: String): ValueIndexItem

        /**
         * Creates a value index item with the given property.
         *
         * @param expression The expression to index. Typically, a property expression.
         * @return The value index item
         */
        public fun expression(expression: Expression): ValueIndexItem
    }
}
