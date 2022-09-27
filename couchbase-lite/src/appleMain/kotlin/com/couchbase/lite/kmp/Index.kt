package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLIndex
import com.udobny.kmp.DelegatedClass

public actual abstract class Index(actual: CBLIndex) : DelegatedClass<CBLIndex>(actual)
