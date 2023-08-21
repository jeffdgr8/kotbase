package kotbase

import cnames.structs.CBLEndpoint
import kotbase.internal.fleece.toFLString
import kotbase.internal.wrapCBLError
import kotlinx.cinterop.CPointer
import libcblite.CBLEndpoint_CreateWithURL
import libcblite.CBLEndpoint_Free
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual class URLEndpoint
internal constructor(
    override val actual: CPointer<CBLEndpoint>,
    public actual val url: String
) : Endpoint {

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        CBLEndpoint_Free(it)
    }

    public actual constructor(url: String) : this(
        try {
            wrapCBLError { error ->
                CBLEndpoint_CreateWithURL(url.toFLString(), error)!!
            }
        } catch (e: CouchbaseLiteException) {
            throw IllegalArgumentException(e.message, e)
        },
        url
    )
}
