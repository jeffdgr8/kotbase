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
package kotbase.ktx

import kotbase.MutableArray

/**
 * Creates a new MutableArray with content from the passed values.
 * Allowed value types are List, Instant, Map, Number, null, String, Array, Blob, and Dictionary.
 * If present, Lists, Arrays, Maps and Dictionaries may contain only the above types.
 *
 * @param values the array content values
 */
public fun mutableArrayOf(vararg values: Any?): MutableArray =
    MutableArray(values.asList())
