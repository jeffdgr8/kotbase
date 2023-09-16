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

import com.couchbase.lite.Function as CBLFunction

public actual object Function {

    public actual fun avg(operand: Expression): Expression =
        Expression(CBLFunction.avg(operand.actual))

    public actual fun count(operand: Expression?): Expression =
        Expression(CBLFunction.count(operand?.actual))

    public actual fun min(operand: Expression): Expression =
        Expression(CBLFunction.min(operand.actual))

    public actual fun max(operand: Expression): Expression =
        Expression(CBLFunction.max(operand.actual))

    public actual fun sum(operand: Expression): Expression =
        Expression(CBLFunction.sum(operand.actual))

    public actual fun abs(operand: Expression): Expression =
        Expression(CBLFunction.abs(operand.actual))

    public actual fun acos(operand: Expression): Expression =
        Expression(CBLFunction.acos(operand.actual))

    public actual fun asin(operand: Expression): Expression =
        Expression(CBLFunction.asin(operand.actual))

    public actual fun atan(operand: Expression): Expression =
        Expression(CBLFunction.atan(operand.actual))

    public actual fun atan2(y: Expression, x: Expression): Expression =
        Expression(CBLFunction.atan2(y.actual, x.actual))

    public actual fun ceil(operand: Expression): Expression =
        Expression(CBLFunction.ceil(operand.actual))

    public actual fun cos(operand: Expression): Expression =
        Expression(CBLFunction.cos(operand.actual))

    public actual fun degrees(operand: Expression): Expression =
        Expression(CBLFunction.degrees(operand.actual))

    public actual fun e(): Expression =
        Expression(CBLFunction.e())

    public actual fun exp(operand: Expression): Expression =
        Expression(CBLFunction.exp(operand.actual))

    public actual fun floor(operand: Expression): Expression =
        Expression(CBLFunction.floor(operand.actual))

    public actual fun ln(operand: Expression): Expression =
        Expression(CBLFunction.ln(operand.actual))

    public actual fun log(operand: Expression): Expression =
        Expression(CBLFunction.log(operand.actual))

    public actual fun pi(): Expression =
        Expression(CBLFunction.pi())

    public actual fun power(base: Expression, exp: Expression): Expression =
        Expression(CBLFunction.power(base.actual, exp.actual))

    public actual fun radians(operand: Expression): Expression =
        Expression(CBLFunction.radians(operand.actual))

    public actual fun round(operand: Expression): Expression =
        Expression(CBLFunction.round(operand.actual))

    public actual fun round(operand: Expression, digits: Expression): Expression =
        Expression(CBLFunction.round(operand.actual, digits.actual))

    public actual fun sign(operand: Expression): Expression =
        Expression(CBLFunction.sign(operand.actual))

    public actual fun sin(operand: Expression): Expression =
        Expression(CBLFunction.sin(operand.actual))

    public actual fun sqrt(operand: Expression): Expression =
        Expression(CBLFunction.sqrt(operand.actual))

    public actual fun tan(operand: Expression): Expression =
        Expression(CBLFunction.tan(operand.actual))

    public actual fun trunc(operand: Expression): Expression =
        Expression(CBLFunction.trunc(operand.actual))

    public actual fun trunc(operand: Expression, digits: Expression): Expression =
        Expression(CBLFunction.trunc(operand.actual, digits.actual))

    public actual fun contains(operand: Expression, substring: Expression): Expression =
        Expression(CBLFunction.contains(operand.actual, substring.actual))

    public actual fun length(operand: Expression): Expression =
        Expression(CBLFunction.length(operand.actual))

    public actual fun lower(operand: Expression): Expression =
        Expression(CBLFunction.lower(operand.actual))

    public actual fun ltrim(operand: Expression): Expression =
        Expression(CBLFunction.ltrim(operand.actual))

    public actual fun rtrim(operand: Expression): Expression =
        Expression(CBLFunction.rtrim(operand.actual))

    public actual fun trim(operand: Expression): Expression =
        Expression(CBLFunction.trim(operand.actual))

    public actual fun upper(operand: Expression): Expression =
        Expression(CBLFunction.upper(operand.actual))

    public actual fun millisToString(operand: Expression): Expression =
        Expression(CBLFunction.millisToString(operand.actual))

    public actual fun millisToUTC(operand: Expression): Expression =
        Expression(CBLFunction.millisToUTC(operand.actual))

    public actual fun stringToMillis(operand: Expression): Expression =
        Expression(CBLFunction.stringToMillis(operand.actual))

    public actual fun stringToUTC(operand: Expression): Expression =
        Expression(CBLFunction.stringToUTC(operand.actual))
}
