package com.udobny.kmp

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
