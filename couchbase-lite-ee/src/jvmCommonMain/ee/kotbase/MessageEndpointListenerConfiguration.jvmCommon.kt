package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.MessageEndpointListenerConfiguration as CBLMessageEndpointListenerConfiguration

public actual class MessageEndpointListenerConfiguration
internal constructor(
    actual: CBLMessageEndpointListenerConfiguration,
    public actual val database: Database
) : DelegatedClass<CBLMessageEndpointListenerConfiguration>(actual) {

    public actual constructor(
        database: Database,
        protocolType: ProtocolType
    ) : this(
        CBLMessageEndpointListenerConfiguration(database.actual, protocolType),
        database
    )

    public actual val protocolType: ProtocolType
        get() = actual.protocolType
}
