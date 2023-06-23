package kotbase

import com.couchbase.lite.TLSIdentity as CBLTLSIdentity

internal expect val TLSIdentity.actual: CBLTLSIdentity

internal expect fun CBLTLSIdentity.asTLSIdentity(): TLSIdentity
