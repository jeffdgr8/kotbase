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
package kotbase.internal

public abstract class DelegatedClass<D : Any>
internal constructor(internal open val actual: D) {

    override fun equals(other: Any?): Boolean =
        actual == (other as? DelegatedClass<*>)?.actual

    override fun hashCode(): Int =
        actual.hashCode()

    override fun toString(): String =
        actual.toString()
}

internal inline fun <reified D : Any> Array<out DelegatedClass<D>>.actuals(): Array<D> =
    map { it.actual }.toTypedArray()

internal inline fun <reified D : Any> Iterable<DelegatedClass<D>>.actuals(): List<D> =
    map { it.actual }

internal inline fun <reified D : Any> Iterable<DelegatedClass<D>>.actualSet(): Set<D> =
    map { it.actual }.toSet()
