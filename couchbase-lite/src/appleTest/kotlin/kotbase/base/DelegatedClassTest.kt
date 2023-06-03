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
        assertEquals(delegate1, delegate2, "delegates should be equal")
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
