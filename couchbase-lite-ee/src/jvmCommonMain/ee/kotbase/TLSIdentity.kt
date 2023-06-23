package kotbase

import com.couchbase.lite.TLSIdentity as CBLTLSIdentity

@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "KotlinRedundantDiagnosticSuppress")
internal expect val TLSIdentity.actual: CBLTLSIdentity

internal expect fun CBLTLSIdentity.asTLSIdentity(): TLSIdentity
