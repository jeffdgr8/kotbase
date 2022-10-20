package com.couchbase.lite.kmp

public actual class PredictionFunction: Expression() {

    public actual fun propertyPath(path: String): Expression =
        predictiveQueryUnsupported()
}
