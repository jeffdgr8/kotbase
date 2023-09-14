package kotbase

import cocoapods.CouchbaseLite.CBLQueryFunction

public actual object Function {

    public actual fun avg(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.avg(operand.actual))

    // TODO: remove Expression.value(".") when nullable in 3.1 (pending iOS as well)
    //  https://forums.couchbase.com/t/function-count-docs-api-clarification/33876
    public actual fun count(operand: Expression?): Expression =
        ExpressionImpl(CBLQueryFunction.count(operand?.actual ?: Expression.value(".").actual))

    public actual fun min(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.min(operand.actual))

    public actual fun max(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.max(operand.actual))

    public actual fun sum(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.sum(operand.actual))

    public actual fun abs(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.abs(operand.actual))

    public actual fun acos(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.acos(operand.actual))

    public actual fun asin(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.asin(operand.actual))

    public actual fun atan(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.atan(operand.actual))

    public actual fun atan2(y: Expression, x: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.atan2(y.actual, x.actual))

    public actual fun ceil(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.ceil(operand.actual))

    public actual fun cos(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.cos(operand.actual))

    public actual fun degrees(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.degrees(operand.actual))

    public actual fun e(): Expression =
        ExpressionImpl(CBLQueryFunction.e())

    public actual fun exp(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.exp(operand.actual))

    public actual fun floor(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.floor(operand.actual))

    public actual fun ln(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.ln(operand.actual))

    public actual fun log(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.log(operand.actual))

    public actual fun pi(): Expression =
        ExpressionImpl(CBLQueryFunction.pi())

    public actual fun power(base: Expression, exp: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.power(base.actual, exp.actual))

    public actual fun radians(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.radians(operand.actual))

    public actual fun round(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.round(operand.actual))

    public actual fun round(operand: Expression, digits: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.round(operand.actual, digits.actual))

    public actual fun sign(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.sign(operand.actual))

    public actual fun sin(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.sin(operand.actual))

    public actual fun sqrt(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.sqrt(operand.actual))

    public actual fun tan(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.tan(operand.actual))

    public actual fun trunc(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.trunc(operand.actual))

    public actual fun trunc(operand: Expression, digits: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.trunc(operand.actual, digits.actual))

    public actual fun contains(operand: Expression, substring: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.contains(operand.actual, substring.actual))

    public actual fun length(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.length(operand.actual))

    public actual fun lower(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.lower(operand.actual))

    public actual fun ltrim(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.ltrim(operand.actual))

    public actual fun rtrim(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.rtrim(operand.actual))

    public actual fun trim(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.trim(operand.actual))

    public actual fun upper(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.upper(operand.actual))

    public actual fun millisToString(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.millisToString(operand.actual))

    public actual fun millisToUTC(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.millisToUTC(operand.actual))

    public actual fun stringToMillis(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.stringToMillis(operand.actual))

    public actual fun stringToUTC(operand: Expression): Expression =
        ExpressionImpl(CBLQueryFunction.stringToUTC(operand.actual))
}
