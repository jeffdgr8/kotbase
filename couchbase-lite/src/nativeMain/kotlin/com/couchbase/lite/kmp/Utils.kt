package com.couchbase.lite.kmp

internal fun invalidTypeError(value: Any) {
    throw IllegalArgumentException(
        "${value::class} is not a valid type. Valid types are simple types and dictionaries and one-dimensional arrays of those types, including MutableDictionary, Dictionary, Map, MutableArray, Array, List, Blob, Date, String, Number, Boolean and null"
    )
}
