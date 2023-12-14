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
package kotbase.util

import kotlinx.cinterop.*

internal inline operator fun <reified T : CStructVar> CPointer<T>?.plus(index: Int): CPointer<T>? =
    interpretCPointer(this.rawValue + index * sizeOf<T>())

internal inline operator fun <reified T : CStructVar> CPointer<T>.get(index: Int): T =
    (this + index)!!.pointed

internal inline fun <reified T : CStructVar, R> CPointer<T>.toList(
    size: Int,
    transform: (CPointer<T>) -> R
): List<R> {
    val array = this
    return buildList(size) {
        repeat(size) { i ->
            add(transform(array[i].ptr))
        }
    }
}

internal inline fun <reified T : Any> CPointer<*>?.to(): T = this!!.asStableRef<T>().get()
