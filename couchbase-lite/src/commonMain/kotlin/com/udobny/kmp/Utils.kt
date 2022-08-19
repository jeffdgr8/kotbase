package com.udobny.kmp

internal inline fun <T, D> T.chain(delegate: D, action: D.() -> Unit): T {
    delegate.action()
    return this
}
