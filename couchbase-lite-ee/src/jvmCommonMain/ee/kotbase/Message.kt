package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.Message as CBLMessage

public actual class Message
internal constructor(actual: CBLMessage) : DelegatedClass<CBLMessage>(actual) {

    public actual fun toData(): ByteArray =
        actual.toData()

    public actual companion object {

        public actual fun fromData(data: ByteArray): Message =
            Message(CBLMessage.fromData(data))
    }
}
