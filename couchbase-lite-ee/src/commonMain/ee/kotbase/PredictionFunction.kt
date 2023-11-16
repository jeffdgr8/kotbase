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
 * **ENTERPRISE EDITION API**
 *
 * Create an expression that refers to one of the properties of the prediction results dictionary.
 */
public expect class PredictionFunction : Expression {

    /**
     * Creates a property expression that refers to a property of the prediction result dictionary.
     *
     * @param path The path to the property.
     * @return The property expression referring to a property of the prediction dictionary result.
     */
    public fun propertyPath(path: String): Expression
}
