package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class Message
internal constructor(actual: com.couchbase.lite.Message) :
    DelegatedClass<com.couchbase.lite.Message>(actual) {

    public actual fun toData(): ByteArray =
        actual.toData()

    public actual companion object {

        public actual fun fromData(data: ByteArray): Message =
            Message(com.couchbase.lite.Message.fromData(data))
    }
}
