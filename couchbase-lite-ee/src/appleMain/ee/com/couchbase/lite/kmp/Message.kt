package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLMessage
import com.udobny.kmp.DelegatedClass
import com.udobny.kmp.ext.toByteArray
import com.udobny.kmp.ext.toNSData

public actual class Message
internal constructor(actual: CBLMessage) :
    DelegatedClass<CBLMessage>(actual) {

    public actual fun toData(): ByteArray =
        actual.toData().toByteArray()

    public actual companion object {

        public actual fun fromData(data: ByteArray): Message =
            Message(CBLMessage.fromData(data.toNSData()))
    }
}
