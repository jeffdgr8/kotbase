package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.Index as CBLIndex

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
public actual abstract class Index
internal constructor(actual: CBLIndex) : DelegatedClass<CBLIndex>(actual)
