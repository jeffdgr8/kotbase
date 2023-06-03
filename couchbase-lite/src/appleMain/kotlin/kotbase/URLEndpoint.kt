package kotbase

import cocoapods.CouchbaseLite.CBLURLEndpoint
import kotbase.base.DelegatedClass
import platform.Foundation.NSURL

public actual class URLEndpoint
internal constructor(override val actual: CBLURLEndpoint) :
    DelegatedClass<CBLURLEndpoint>(actual), Endpoint {

    public actual constructor(url: String) : this(CBLURLEndpoint(validate(url)))

    public actual val url: String
        get() = actual.url.path!!

    private companion object {

        private const val SCHEME_STD = "ws"
        private const val SCHEME_TLS = "wss"

        private fun validate(url: String): NSURL {
            val nsUrl = NSURL.URLWithString(url)!!

            val scheme = nsUrl.scheme
            if (!((SCHEME_STD == scheme) || (SCHEME_TLS == scheme))) {
                throw IllegalArgumentException("Invalid scheme for URLEndpoint url ($url). It must be either 'ws:' or 'wss:'.")
            }

            if (nsUrl.password != null) {
                throw IllegalArgumentException("Embedded credentials in a URL (username:password@url) are not allowed. Use the BasicAuthenticator class instead.")
            }

            return nsUrl
        }
    }
}
