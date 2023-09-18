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

import kotlin.test.Test
import kotlin.test.assertEquals

class DelegatedClassTest {

    private class MyObject(val value: Int) {

        override fun equals(other: Any?): Boolean {
            if (other === this) {
                return true
            }
            if (other is MyObject && other.value == value) {
                return true
            }
            return false
        }

        override fun hashCode(): Int {
            return value
        }

        override fun toString(): String {
            return "My value is $value"
        }
    }

    private class MyDelegatedObject(actual: MyObject) : DelegatedClass<MyObject>(actual)

    @Test
    fun test_equals() {
        val obj1 = MyObject(42)
        val delegate1 = MyDelegatedObject(obj1)
        val obj2 = MyObject(42)
        val delegate2 = MyDelegatedObject(obj2)
        assertEquals(obj1, obj2, "objects should be equal")
        assertEquals(obj2, obj1, "objects should be transitively equal")
        assertEquals(delegate1, delegate2, "delegates should be equal")
        assertEquals(delegate2, delegate1, "delegates should be transitively equal")
    }

    @Test
    fun test_hashCode() {
        val obj = MyObject(42)
        val delegate = MyDelegatedObject(obj)
        assertEquals(obj.hashCode(), delegate.hashCode())
    }

    @Test
    fun test_toString() {
        val obj = MyObject(42)
        val delegate = MyDelegatedObject(obj)
        assertEquals(obj.toString(), delegate.toString())
    }
}
