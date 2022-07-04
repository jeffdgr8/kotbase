package com.udobny.kmm

internal inline fun <T, D> T.chain(delegate: D, action: D.() -> Unit): T {
    delegate.action()
    return this
}
