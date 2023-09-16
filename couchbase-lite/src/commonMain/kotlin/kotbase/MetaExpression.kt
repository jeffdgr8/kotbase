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
 * A meta property expression.
 */
public expect class MetaExpression : Expression {

    /**
     * Specifies an alias name of the data source to query the data from.
     *
     * @param fromAlias The data source alias name.
     * @return The Meta expression with the given alias name specified.
     */
    public fun from(fromAlias: String): Expression
}
