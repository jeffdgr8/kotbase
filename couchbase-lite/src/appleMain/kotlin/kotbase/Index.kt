package kotbase

import cocoapods.CouchbaseLite.CBLIndex
import kotbase.base.DelegatedClass

public actual abstract class Index(actual: CBLIndex) : DelegatedClass<CBLIndex>(actual)
