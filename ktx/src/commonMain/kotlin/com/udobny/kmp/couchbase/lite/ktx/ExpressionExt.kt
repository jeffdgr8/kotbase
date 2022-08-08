@file:Suppress("NOTHING_TO_INLINE")

package com.udobny.kmp.couchbase.lite.ktx

import com.couchbase.lite.kmp.ArrayFunction
import com.couchbase.lite.kmp.Expression
import com.udobny.kmp.ext.toNativeDate
import kotlinx.datetime.Instant

/**
 * Create a between expression that evaluates whether or not the current
 * expression is between the given dates inclusively.
 *
 * @param lower the inclusive lower bound date
 * @param upper the inclusive upper bound date
 * @return a between expression.
 */
public fun Expression.between(lower: Instant, upper: Instant): Expression =
    between(Expression.value(lower.toNativeDate()), Expression.value(upper.toNativeDate()))

/**
 * Creates an ARRAY_CONTAINS(expr, value) function that checks whether
 * the current array expression contains the given value or not.
 *
 * @param value The value to search for in the array expression
 * @return The ARRAY_CONTAINS(expr, value) function
 *
 * @see notContains
 */
public inline infix fun Expression.contains(value: String): Expression =
    ArrayFunction.contains(this, Expression.property(value))

/**
 * Creates a NOT ARRAY_CONTAINS(expr, value) expression that checks
 * whether the current array expression contains the given value or
 * not and returns the negated result.
 *
 * @param value The value to search for in the array expression
 * @return The NOT ARRAY_CONTAINS(expr, value) expression
 *
 * @see contains
 */
public inline infix fun Expression.notContains(value: String): Expression =
    Expression.not(contains(value))
