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
package kotbase.base

import kotlinx.cinterop.convert
import platform.darwin.NSObject
import platform.darwin.NSUInteger
import kotlin.test.Test
import kotlin.test.assertEquals

class DelegatedClassTest {

    private class MyNSObject(val value: Int) : NSObject() {

        override fun isEqual(`object`: Any?): Boolean {
            if (`object` === this) {
                return true
            }
            if (`object` is MyNSObject && `object`.value == value) {
                return true
            }
            return false
        }

        override fun hash(): NSUInteger {
            return value.convert()
        }

        override fun description(): String {
            return "My value is $value"
        }
    }

    private class MyDelegatedObject(actual: MyNSObject) : DelegatedClass<MyNSObject>(actual)

    @Test
    fun test_equals() {
        val obj1 = MyNSObject(42)
        val delegate1 = MyDelegatedObject(obj1)
        val obj2 = MyNSObject(42)
        val delegate2 = MyDelegatedObject(obj2)
        assertEquals(obj1, obj2, "objects should be equal")
        assertEquals(obj2, obj1, "objects should be transitively equal")
        assertEquals(delegate1, delegate2, "delegates should be equal")
        assertEquals(delegate2, delegate1, "delegates should be transitively equal")
    }

    @Test
    fun test_hashCode() {
        val obj = MyNSObject(42)
        val delegate = MyDelegatedObject(obj)
        assertEquals(obj.hash, delegate.hashCode().convert())
    }

    @Test
    fun test_toString() {
        val obj = MyNSObject(42)
        val delegate = MyDelegatedObject(obj)
        assertEquals(obj.description, delegate.toString())
    }
}
