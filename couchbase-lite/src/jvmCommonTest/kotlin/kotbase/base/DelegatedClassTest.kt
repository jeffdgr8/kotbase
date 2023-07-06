package kotbase.base

import org.junit.Test
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
