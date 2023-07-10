package kotbase

import cocoapods.CouchbaseLite.CBLProtocolType
import cocoapods.CouchbaseLite.CBLProtocolType.kCBLProtocolTypeByteStream
import cocoapods.CouchbaseLite.CBLProtocolType.kCBLProtocolTypeMessageStream

public actual enum class ProtocolType {
    MESSAGE_STREAM,
    BYTE_STREAM;

    public val actual: CBLProtocolType
        get() = when (this) {
            MESSAGE_STREAM -> kCBLProtocolTypeMessageStream
            BYTE_STREAM -> kCBLProtocolTypeByteStream
        }

    internal companion object {

        internal fun from(protocolType: CBLProtocolType): ProtocolType = when (protocolType) {
            kCBLProtocolTypeMessageStream -> MESSAGE_STREAM
            kCBLProtocolTypeByteStream -> BYTE_STREAM
            else -> error("Unexpected CBLProtocolType ($protocolType)")
        }
    }
}
