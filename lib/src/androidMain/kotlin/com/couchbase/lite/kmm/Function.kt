package com.couchbase.lite.kmm

public actual object Function {

    public actual fun avg(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.avg(operand.actual))

    // TODO: remove Expression.value(".") when nullable in 3.1 (pending iOS as well)
    //  https://forums.couchbase.com/t/function-count-docs-api-clarification/33876
    public actual fun count(operand: Expression?): Expression =
        Expression(
            com.couchbase.lite.Function.count(operand?.actual ?: Expression.value(".").actual)
        )

    public actual fun min(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.min(operand.actual))

    public actual fun max(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.max(operand.actual))

    public actual fun sum(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.sum(operand.actual))

    public actual fun abs(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.abs(operand.actual))

    public actual fun acos(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.acos(operand.actual))

    public actual fun asin(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.asin(operand.actual))

    public actual fun atan(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.atan(operand.actual))

    public actual fun atan2(y: Expression, x: Expression): Expression =
        Expression(com.couchbase.lite.Function.atan2(y.actual, x.actual))

    public actual fun ceil(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.ceil(operand.actual))

    public actual fun cos(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.cos(operand.actual))

    public actual fun degrees(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.degrees(operand.actual))

    public actual fun e(): Expression =
        Expression(com.couchbase.lite.Function.e())

    public actual fun exp(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.exp(operand.actual))

    public actual fun floor(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.floor(operand.actual))

    public actual fun ln(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.ln(operand.actual))

    public actual fun log(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.log(operand.actual))

    public actual fun pi(): Expression =
        Expression(com.couchbase.lite.Function.pi())

    public actual fun power(base: Expression, exp: Expression): Expression =
        Expression(com.couchbase.lite.Function.power(base.actual, exp.actual))

    public actual fun radians(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.radians(operand.actual))

    public actual fun round(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.round(operand.actual))

    public actual fun round(operand: Expression, digits: Expression): Expression =
        Expression(com.couchbase.lite.Function.round(operand.actual, digits.actual))

    public actual fun sign(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.sign(operand.actual))

    public actual fun sin(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.sin(operand.actual))

    public actual fun sqrt(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.sqrt(operand.actual))

    public actual fun tan(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.tan(operand.actual))

    public actual fun trunc(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.trunc(operand.actual))

    public actual fun trunc(operand: Expression, digits: Expression): Expression =
        Expression(com.couchbase.lite.Function.trunc(operand.actual, digits.actual))

    public actual fun contains(operand: Expression, substring: Expression): Expression =
        Expression(com.couchbase.lite.Function.contains(operand.actual, substring.actual))

    public actual fun length(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.length(operand.actual))

    public actual fun lower(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.lower(operand.actual))

    public actual fun ltrim(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.ltrim(operand.actual))

    public actual fun rtrim(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.rtrim(operand.actual))

    public actual fun trim(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.trim(operand.actual))

    public actual fun upper(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.upper(operand.actual))

    public actual fun millisToString(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.millisToString(operand.actual))

    public actual fun millisToUTC(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.millisToUTC(operand.actual))

    public actual fun stringToMillis(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.stringToMillis(operand.actual))

    public actual fun stringToUTC(operand: Expression): Expression =
        Expression(com.couchbase.lite.Function.stringToUTC(operand.actual))
}
