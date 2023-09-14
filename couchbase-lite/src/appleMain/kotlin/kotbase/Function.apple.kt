package kotbase

import cocoapods.CouchbaseLite.CBLQueryFunction

public actual object Function {

    public actual fun avg(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.avg(operand.actual))

    // TODO: remove Expression.value(".") when nullable in 3.1 (pending iOS as well)
    //  https://forums.couchbase.com/t/function-count-docs-api-clarification/33876
    public actual fun count(operand: Expression?): Expression =
        DelegatedExpression(CBLQueryFunction.count(operand?.actual ?: Expression.value(".").actual))

    public actual fun min(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.min(operand.actual))

    public actual fun max(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.max(operand.actual))

    public actual fun sum(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.sum(operand.actual))

    public actual fun abs(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.abs(operand.actual))

    public actual fun acos(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.acos(operand.actual))

    public actual fun asin(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.asin(operand.actual))

    public actual fun atan(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.atan(operand.actual))

    public actual fun atan2(y: Expression, x: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.atan2(y.actual, x.actual))

    public actual fun ceil(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.ceil(operand.actual))

    public actual fun cos(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.cos(operand.actual))

    public actual fun degrees(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.degrees(operand.actual))

    public actual fun e(): Expression =
        DelegatedExpression(CBLQueryFunction.e())

    public actual fun exp(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.exp(operand.actual))

    public actual fun floor(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.floor(operand.actual))

    public actual fun ln(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.ln(operand.actual))

    public actual fun log(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.log(operand.actual))

    public actual fun pi(): Expression =
        DelegatedExpression(CBLQueryFunction.pi())

    public actual fun power(base: Expression, exp: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.power(base.actual, exp.actual))

    public actual fun radians(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.radians(operand.actual))

    public actual fun round(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.round(operand.actual))

    public actual fun round(operand: Expression, digits: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.round(operand.actual, digits.actual))

    public actual fun sign(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.sign(operand.actual))

    public actual fun sin(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.sin(operand.actual))

    public actual fun sqrt(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.sqrt(operand.actual))

    public actual fun tan(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.tan(operand.actual))

    public actual fun trunc(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.trunc(operand.actual))

    public actual fun trunc(operand: Expression, digits: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.trunc(operand.actual, digits.actual))

    public actual fun contains(operand: Expression, substring: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.contains(operand.actual, substring.actual))

    public actual fun length(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.length(operand.actual))

    public actual fun lower(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.lower(operand.actual))

    public actual fun ltrim(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.ltrim(operand.actual))

    public actual fun rtrim(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.rtrim(operand.actual))

    public actual fun trim(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.trim(operand.actual))

    public actual fun upper(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.upper(operand.actual))

    public actual fun millisToString(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.millisToString(operand.actual))

    public actual fun millisToUTC(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.millisToUTC(operand.actual))

    public actual fun stringToMillis(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.stringToMillis(operand.actual))

    public actual fun stringToUTC(operand: Expression): Expression =
        DelegatedExpression(CBLQueryFunction.stringToUTC(operand.actual))
}
