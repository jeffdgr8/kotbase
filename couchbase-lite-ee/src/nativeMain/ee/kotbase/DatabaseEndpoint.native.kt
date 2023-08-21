package kotbase

import cnames.structs.CBLEndpoint
import kotlinx.cinterop.CPointer
import libcblite.CBLEndpoint_CreateWithLocalDB
import libcblite.CBLEndpoint_Free
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual class DatabaseEndpoint
internal constructor(
    override val actual: CPointer<CBLEndpoint>,
    public actual val database: Database
) : Endpoint {

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        CBLEndpoint_Free(it)
    }

    public actual constructor(database: Database) : this(
        CBLEndpoint_CreateWithLocalDB(database.actual)!!,
        database
    )
}
