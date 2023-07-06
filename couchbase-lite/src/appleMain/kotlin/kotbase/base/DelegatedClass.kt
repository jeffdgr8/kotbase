package kotbase.base

import platform.darwin.NSObject

public abstract class DelegatedClass<D : NSObject>
internal constructor(override val actual: D) : AbstractDelegatedClass<D>()

public abstract class AbstractDelegatedClass<D : NSObject> {

    public abstract val actual: D

    override fun equals(other: Any?): Boolean =
        actual.isEqual((other as? AbstractDelegatedClass<*>)?.actual)

    override fun hashCode(): Int =
        actual.hash.toInt()

    override fun toString(): String =
        actual.description ?: super.toString()
}

internal inline fun <reified D : NSObject> Array<out DelegatedClass<D>>.actuals(): List<D> =
    map { it.actual }
