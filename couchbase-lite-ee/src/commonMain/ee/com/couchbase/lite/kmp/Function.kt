package com.couchbase.lite.kmp

/**
 * **ENTERPRISE EDITION API**
 *
 * Creates prediction function with the given model name and input. When running a query with
 * the prediction function, the corresponding predictive model registered to CouchbaseLite
 * Database class will be called with the given input to predict the result.
 *
 * The prediction result returned by the predictive model will be in a form dictionary object.
 * To create an expression that refers to a property in the prediction result,
 * the propertyPath(path) method of the created PredictionFunction object
 * can be used.
 *
 * @param model The predictive model name registered to the CouchbaseLite Database.
 * @param input The expression evaluated to a dictionary.
 * @return A PredictionFunction object.
 */
public expect fun Function.prediction(model: String, input: Expression): PredictionFunction

/**
 * **ENTERPRISE EDITION API**
 *
 * Creates a function that returns the euclidean distance between the two input vectors.
 * The result is a non-negative floating-point number. The expression1 and expression2 must be
 * arrays of numbers, and must be the same length.
 *
 * @param expression1 The expression evaluated to an arrays of numbers.
 * @param expression2 The expression evaluated to an arrays of numbers.
 * @return The euclidean distance between two given input vectors.
 */
public expect fun Function.euclideanDistance(
    expression1: Expression,
    expression2: Expression
): Expression

/**
 * **ENTERPRISE EDITION API**
 *
 * Creates a function that returns the squared euclidean distance between the two input vectors.
 * The result is a non-negative floating-point number. The expression1 and expression2 must be
 * arrays of numbers, and must be the same length.
 *
 * @param expression1 The expression evaluated to an arrays of numbers.
 * @param expression2 The expression evaluated to an arrays of numbers.
 * @return The squared euclidean distance between two given input vectors.
 */
public expect fun Function.squaredEuclideanDistance(
    expression1: Expression,
    expression2: Expression
): Expression

/**
 * **ENTERPRISE EDITION API**
 *
 * Creates a function that returns the cosine distance which one minus the cosine similarity
 * between the two input vectors. The result is a floating-point number ranges from −1.0 to 1.0.
 * The expression1 and expression2 must be arrays of numbers, and must be the same length.
 *
 * @param expression1 The expression evaluated to an arrays of numbers.
 * @param expression2 The expression evaluated to an arrays of numbers.
 * @return The cosine distance between two given input vectors.
 */
public expect fun Function.cosineDistance(
    expression1: Expression,
    expression2: Expression
): Expression
