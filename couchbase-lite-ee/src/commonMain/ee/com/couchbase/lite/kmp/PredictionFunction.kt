package com.couchbase.lite.kmp

/**
 * **ENTERPRISE EDITION API**
 *
 * PredictionFunction that allows to create an expression that
 * refers to one of the properties of the prediction result dictionary.
 */
public expect class PredictionFunction : Expression {

    /**
     * Creates a property expression that refers to a property of the prediction result dictionary.
     *
     * @param path The path to the property.
     * @return The property expression referring to a property of the prediction dictionary result.
     */
    public fun propertyPath(path: String): Expression
}
