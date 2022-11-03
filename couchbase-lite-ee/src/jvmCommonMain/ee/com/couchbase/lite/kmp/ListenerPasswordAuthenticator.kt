package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class ListenerPasswordAuthenticator
internal constructor(override val actual: com.couchbase.lite.ListenerPasswordAuthenticator) :
    DelegatedClass<com.couchbase.lite.ListenerPasswordAuthenticator>(actual),
    ListenerAuthenticator {

    public actual constructor(delegate: ListenerPasswordAuthenticatorDelegate) : this(
        com.couchbase.lite.ListenerPasswordAuthenticator(delegate.convert())
    )
}
