package kotbase

import cocoapods.CouchbaseLite.CBLMessageEndpointListenerConfiguration
import kotbase.base.DelegatedClass

public actual class MessageEndpointListenerConfiguration
internal constructor(
    actual: CBLMessageEndpointListenerConfiguration,
    public actual val database: Database
) : DelegatedClass<CBLMessageEndpointListenerConfiguration>(actual) {

    public actual constructor(
        database: Database,
        protocolType: ProtocolType
    ) : this(
        CBLMessageEndpointListenerConfiguration(database.actual, protocolType.actual),
        database
    )

    public actual val protocolType: ProtocolType
        get() = ProtocolType.from(actual.protocolType)
}
