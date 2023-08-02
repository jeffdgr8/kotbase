package kotbase

import cocoapods.CouchbaseLite.CBLMessage
import kotbase.base.DelegatedClass
import kotbase.ext.toByteArray
import kotbase.ext.toNSData

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
