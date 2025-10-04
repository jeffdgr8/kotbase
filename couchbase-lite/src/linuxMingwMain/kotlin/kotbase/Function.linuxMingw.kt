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
package kotbase

public actual object Function {

    public actual fun avg(operand: Expression): Expression =
        sexpr("AVG()", operand)

    public actual fun count(operand: Expression?): Expression =
        sexpr("COUNT()", operand ?: Expression.value("."))

    public actual fun min(operand: Expression): Expression =
        sexpr("MIN()", operand)

    public actual fun max(operand: Expression): Expression =
        sexpr("MAX()", operand)

    public actual fun sum(operand: Expression): Expression =
        sexpr("SUM()", operand)

    public actual fun abs(operand: Expression): Expression =
        sexpr("ABS()", operand)

    public actual fun acos(operand: Expression): Expression =
        sexpr("ACOS()", operand)

    public actual fun asin(operand: Expression): Expression =
        sexpr("ASIN()", operand)

    public actual fun atan(operand: Expression): Expression =
        sexpr("ATAN()", operand)

    public actual fun atan2(y: Expression, x: Expression): Expression =
        expr("ATAN2()", y, x)

    public actual fun ceil(operand: Expression): Expression =
        sexpr("CEIL()", operand)

    public actual fun cos(operand: Expression): Expression =
        sexpr("COS()", operand)

    public actual fun degrees(operand: Expression): Expression =
        sexpr("DEGREES()", operand)

    public actual fun e(): Expression =
        expr("E()")

    public actual fun exp(operand: Expression): Expression =
        sexpr("EXP()", operand)

    public actual fun floor(operand: Expression): Expression =
        sexpr("FLOOR()", operand)

    public actual fun ln(operand: Expression): Expression =
        sexpr("LN()", operand)

    public actual fun log(operand: Expression): Expression =
        sexpr("LOG()", operand)

    public actual fun pi(): Expression =
        expr("PI()")

    public actual fun power(base: Expression, exp: Expression): Expression =
        expr("POWER()", base, exp)

    public actual fun radians(operand: Expression): Expression =
        sexpr("RADIANS()", operand)

    public actual fun round(operand: Expression): Expression =
        sexpr("ROUND()", operand)

    public actual fun round(operand: Expression, digits: Expression): Expression =
        expr("ROUND()", operand, digits)

    public actual fun sign(operand: Expression): Expression =
        sexpr("SIGN()", operand)

    public actual fun sin(operand: Expression): Expression =
        sexpr("SIN()", operand)

    public actual fun sqrt(operand: Expression): Expression =
        sexpr("SQRT()", operand)

    public actual fun tan(operand: Expression): Expression =
        sexpr("TAN()", operand)

    public actual fun trunc(operand: Expression): Expression =
        sexpr("TRUNC()", operand)

    public actual fun trunc(operand: Expression, digits: Expression): Expression =
        expr("TRUNC()", operand, digits)

    public actual fun contains(operand: Expression, substring: Expression): Expression =
        expr("CONTAINS()", operand, substring)

    public actual fun length(operand: Expression): Expression =
        sexpr("LENGTH()", operand)

    public actual fun lower(operand: Expression): Expression =
        sexpr("LOWER()", operand)

    public actual fun ltrim(operand: Expression): Expression =
        sexpr("LTRIM()", operand)

    public actual fun rtrim(operand: Expression): Expression =
        sexpr("RTRIM()", operand)

    public actual fun trim(operand: Expression): Expression =
        sexpr("TRIM()", operand)

    public actual fun upper(operand: Expression): Expression =
        sexpr("UPPER()", operand)

    public actual fun millisToString(operand: Expression): Expression =
        sexpr("MILLIS_TO_STR()", operand)

    public actual fun millisToUTC(operand: Expression): Expression =
        sexpr("MILLIS_TO_UTC()", operand)

    public actual fun stringToMillis(operand: Expression): Expression =
        sexpr("STR_TO_MILLIS()", operand)

    public actual fun stringToUTC(operand: Expression): Expression =
        sexpr("STR_TO_UTC()", operand)

    private fun sexpr(expr: String, op: Expression): Expression.FunctionExpression =
        Expression.FunctionExpression(expr, listOf(op))

    private fun expr(expr: String, vararg ops: Expression): Expression.FunctionExpression =
        Expression.FunctionExpression(expr, ops.asList())
}
