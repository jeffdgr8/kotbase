package kotbase

import com.couchbase.lite.MessageEndpointDelegate as CBLMessageEndpointDelegate

internal fun MessageEndpointDelegate.convert(): CBLMessageEndpointDelegate {
    return CBLMessageEndpointDelegate { endpoint ->
        invoke(MessageEndpoint(endpoint, this)).convert()
    }
}
