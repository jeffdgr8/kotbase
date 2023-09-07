package kotbase

import cocoapods.CouchbaseLite.CBLIndex
import kotbase.base.DelegatedClass

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
public actual abstract class Index
internal constructor(actual: CBLIndex) : DelegatedClass<CBLIndex>(actual)
