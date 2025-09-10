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
import kotlin.time.Instant

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

public operator fun Expression.times(expression: Expression): Expression = multiply(expression)
public operator fun Expression.times(value: Int): Expression = multiply(Expression.intValue(value))
public operator fun Expression.times(value: Long): Expression = multiply(Expression.longValue(value))
public operator fun Expression.times(value: Float): Expression = multiply(Expression.floatValue(value))
public operator fun Expression.times(value: Double): Expression = multiply(Expression.doubleValue(value))

public operator fun Expression.div(expression: Expression): Expression = divide(expression)
public operator fun Expression.div(value: Int): Expression = divide(Expression.intValue(value))
public operator fun Expression.div(value: Long): Expression = divide(Expression.longValue(value))
public operator fun Expression.div(value: Float): Expression = divide(Expression.floatValue(value))
public operator fun Expression.div(value: Double): Expression = divide(Expression.doubleValue(value))

public operator fun Expression.rem(expression: Expression): Expression = modulo(expression)
public operator fun Expression.rem(value: Int): Expression = modulo(Expression.intValue(value))
public operator fun Expression.rem(value: Long): Expression = modulo(Expression.longValue(value))
public operator fun Expression.rem(value: Float): Expression = modulo(Expression.floatValue(value))
public operator fun Expression.rem(value: Double): Expression = modulo(Expression.doubleValue(value))

public operator fun Expression.plus(expression: Expression): Expression = add(expression)
public operator fun Expression.plus(value: Int): Expression = add(Expression.intValue(value))
public operator fun Expression.plus(value: Long): Expression = add(Expression.longValue(value))
public operator fun Expression.plus(value: Float): Expression = add(Expression.floatValue(value))
public operator fun Expression.plus(value: Double): Expression = add(Expression.doubleValue(value))

public operator fun Expression.minus(expression: Expression): Expression = subtract(expression)
public operator fun Expression.minus(value: Int): Expression = subtract(Expression.intValue(value))
public operator fun Expression.minus(value: Long): Expression = subtract(Expression.longValue(value))
public operator fun Expression.minus(value: Float): Expression = subtract(Expression.floatValue(value))
public operator fun Expression.minus(value: Double): Expression = subtract(Expression.doubleValue(value))
