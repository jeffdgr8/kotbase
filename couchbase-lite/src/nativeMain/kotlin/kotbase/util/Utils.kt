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
