/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")

package kotbase.ktx

import kotbase.ArrayFunction
import kotbase.Expression
import kotlinx.datetime.Instant

/**
 * Create a between expression that evaluates whether or not the current
 * expression is between the given dates inclusively.
 *
 * @param lower the inclusive lower bound date
 * @param upper the inclusive upper bound date
 * @return a between expression.
 */
public inline fun Expression.between(lower: Instant, upper: Instant): Expression =
    between(Expression.value(lower), Expression.value(upper))

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