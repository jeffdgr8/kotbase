package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.*

public actual object Function {

    public actual fun avg(operand: Expression): Expression =
        Expression(CBLQueryFunction.avg(operand.actual))

    // TODO: remove Expression.value(".") when nullable in 3.1 (pending iOS as well)
    //  https://forums.couchbase.com/t/function-count-docs-api-clarification/33876
    public actual fun count(operand: Expression?): Expression =
        Expression(CBLQueryFunction.count(operand?.actual ?: Expression.value(".").actual))

    public actual fun min(operand: Expression): Expression =
        Expression(CBLQueryFunction.min(operand.actual))

    public actual fun max(operand: Expression): Expression =
        Expression(CBLQueryFunction.max(operand.actual))

    public actual fun sum(operand: Expression): Expression =
        Expression(CBLQueryFunction.sum(operand.actual))

    public actual fun abs(operand: Expression): Expression =
        Expression(CBLQueryFunction.abs(operand.actual))

    public actual fun acos(operand: Expression): Expression =
        Expression(CBLQueryFunction.acos(operand.actual))

    public actual fun asin(operand: Expression): Expression =
        Expression(CBLQueryFunction.asin(operand.actual))

    public actual fun atan(operand: Expression): Expression =
        Expression(CBLQueryFunction.atan(operand.actual))

    public actual fun atan2(y: Expression, x: Expression): Expression =
        Expression(CBLQueryFunction.atan2(y.actual, x.actual))

    public actual fun ceil(operand: Expression): Expression =
        Expression(CBLQueryFunction.ceil(operand.actual))

    public actual fun cos(operand: Expression): Expression =
        Expression(CBLQueryFunction.cos(operand.actual))

    public actual fun degrees(operand: Expression): Expression =
        Expression(CBLQueryFunction.degrees(operand.actual))

    public actual fun e(): Expression =
        Expression(CBLQueryFunction.e())

    public actual fun exp(operand: Expression): Expression =
        Expression(CBLQueryFunction.exp(operand.actual))

    public actual fun floor(operand: Expression): Expression =
        Expression(CBLQueryFunction.floor(operand.actual))

    public actual fun ln(operand: Expression): Expression =
        Expression(CBLQueryFunction.ln(operand.actual))

    public actual fun log(operand: Expression): Expression =
        Expression(CBLQueryFunction.log(operand.actual))

    public actual fun pi(): Expression =
        Expression(CBLQueryFunction.pi())

    public actual fun power(base: Expression, exp: Expression): Expression =
        Expression(CBLQueryFunction.power(base.actual, exp.actual))

    public actual fun radians(operand: Expression): Expression =
        Expression(CBLQueryFunction.radians(operand.actual))

    public actual fun round(operand: Expression): Expression =
        Expression(CBLQueryFunction.round(operand.actual))

    public actual fun round(operand: Expression, digits: Expression): Expression =
        Expression(CBLQueryFunction.round(operand.actual, digits.actual))

    public actual fun sign(operand: Expression): Expression =
        Expression(CBLQueryFunction.sign(operand.actual))

    public actual fun sin(operand: Expression): Expression =
        Expression(CBLQueryFunction.sin(operand.actual))

    public actual fun sqrt(operand: Expression): Expression =
        Expression(CBLQueryFunction.sqrt(operand.actual))

    public actual fun tan(operand: Expression): Expression =
        Expression(CBLQueryFunction.tan(operand.actual))

    public actual fun trunc(operand: Expression): Expression =
        Expression(CBLQueryFunction.trunc(operand.actual))

    public actual fun trunc(operand: Expression, digits: Expression): Expression =
        Expression(CBLQueryFunction.trunc(operand.actual, digits.actual))

    public actual fun contains(operand: Expression, substring: Expression): Expression =
        Expression(CBLQueryFunction.contains(operand.actual, substring.actual))

    public actual fun length(operand: Expression): Expression =
        Expression(CBLQueryFunction.length(operand.actual))

    public actual fun lower(operand: Expression): Expression =
        Expression(CBLQueryFunction.lower(operand.actual))

    public actual fun ltrim(operand: Expression): Expression =
        Expression(CBLQueryFunction.ltrim(operand.actual))

    public actual fun rtrim(operand: Expression): Expression =
        Expression(CBLQueryFunction.rtrim(operand.actual))

    public actual fun trim(operand: Expression): Expression =
        Expression(CBLQueryFunction.trim(operand.actual))

    public actual fun upper(operand: Expression): Expression =
        Expression(CBLQueryFunction.upper(operand.actual))

    public actual fun millisToString(operand: Expression): Expression =
        Expression(CBLQueryFunction.millisToString(operand.actual))

    public actual fun millisToUTC(operand: Expression): Expression =
        Expression(CBLQueryFunction.millisToUTC(operand.actual))

    public actual fun stringToMillis(operand: Expression): Expression =
        Expression(CBLQueryFunction.stringToMillis(operand.actual))

    public actual fun stringToUTC(operand: Expression): Expression =
        Expression(CBLQueryFunction.stringToUTC(operand.actual))
}
