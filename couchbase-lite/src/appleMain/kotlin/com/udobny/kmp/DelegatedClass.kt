package com.udobny.kmp

import platform.darwin.NSObject

public abstract class DelegatedClass<D : NSObject>
internal constructor(override val actual: D) : AbstractDelegatedClass<D>()

public abstract class AbstractDelegatedClass<D : NSObject> {

    internal abstract val actual: D

    override fun equals(other: Any?): Boolean =
        actual.isEqual((other as? DelegatedClass<*>)?.actual)

    override fun hashCode(): Int =
        actual.hash.toInt()

    override fun toString(): String =
        actual.description ?: ""
}

internal inline fun <reified D : NSObject> Array<out DelegatedClass<D>>.actuals(): List<D> =
    map { it.actual }
