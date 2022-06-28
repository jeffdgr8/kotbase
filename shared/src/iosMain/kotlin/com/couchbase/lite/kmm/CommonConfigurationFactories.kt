@file:Suppress("NAME_SHADOWING")

package com.couchbase.lite.kmm

// TODO: https://forums.couchbase.com/t/cblvalueindexconfiguration-and-cblfulltextindexconfiguration-missing-from-objc-framework-for-x86-64/33815
//public actual fun FullTextIndexConfiguration?.create(
//    vararg expressions: String
//): FullTextIndexConfiguration {
//    val expressions = if (expressions.isNotEmpty()) {
//        expressions
//    } else {
//        @Suppress("UNCHECKED_CAST")
//        (this?.actual?.expressions as List<String>?)?.toTypedArray()
//    } ?: error("Must specify an expression")
//    return FullTextIndexConfiguration(
//        *expressions
//    )
//}
//
//public actual fun ValueIndexConfiguration?.create(
//    vararg expressions: String
//): ValueIndexConfiguration {
//    val expressions = if (expressions.isNotEmpty()) {
//        expressions
//    } else {
//        @Suppress("UNCHECKED_CAST")
//        (this?.actual?.expressions as List<String>?)?.toTypedArray()
//    } ?: error("Must specify an expression")
//    return ValueIndexConfiguration(
//        *expressions
//    )
//}
