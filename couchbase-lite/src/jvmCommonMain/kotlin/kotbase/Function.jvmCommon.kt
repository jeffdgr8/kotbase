package kotbase

import com.couchbase.lite.Function as CBLFunction

public actual object Function {

    public actual fun avg(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.avg(operand.actual))

    public actual fun count(operand: Expression?): Expression =
        DelegatedExpression(CBLFunction.count(operand?.actual))

    public actual fun min(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.min(operand.actual))

    public actual fun max(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.max(operand.actual))

    public actual fun sum(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.sum(operand.actual))

    public actual fun abs(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.abs(operand.actual))

    public actual fun acos(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.acos(operand.actual))

    public actual fun asin(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.asin(operand.actual))

    public actual fun atan(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.atan(operand.actual))

    public actual fun atan2(y: Expression, x: Expression): Expression =
        DelegatedExpression(CBLFunction.atan2(y.actual, x.actual))

    public actual fun ceil(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.ceil(operand.actual))

    public actual fun cos(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.cos(operand.actual))

    public actual fun degrees(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.degrees(operand.actual))

    public actual fun e(): Expression =
        DelegatedExpression(CBLFunction.e())

    public actual fun exp(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.exp(operand.actual))

    public actual fun floor(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.floor(operand.actual))

    public actual fun ln(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.ln(operand.actual))

    public actual fun log(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.log(operand.actual))

    public actual fun pi(): Expression =
        DelegatedExpression(CBLFunction.pi())

    public actual fun power(base: Expression, exp: Expression): Expression =
        DelegatedExpression(CBLFunction.power(base.actual, exp.actual))

    public actual fun radians(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.radians(operand.actual))

    public actual fun round(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.round(operand.actual))

    public actual fun round(operand: Expression, digits: Expression): Expression =
        DelegatedExpression(CBLFunction.round(operand.actual, digits.actual))

    public actual fun sign(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.sign(operand.actual))

    public actual fun sin(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.sin(operand.actual))

    public actual fun sqrt(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.sqrt(operand.actual))

    public actual fun tan(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.tan(operand.actual))

    public actual fun trunc(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.trunc(operand.actual))

    public actual fun trunc(operand: Expression, digits: Expression): Expression =
        DelegatedExpression(CBLFunction.trunc(operand.actual, digits.actual))

    public actual fun contains(operand: Expression, substring: Expression): Expression =
        DelegatedExpression(CBLFunction.contains(operand.actual, substring.actual))

    public actual fun length(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.length(operand.actual))

    public actual fun lower(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.lower(operand.actual))

    public actual fun ltrim(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.ltrim(operand.actual))

    public actual fun rtrim(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.rtrim(operand.actual))

    public actual fun trim(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.trim(operand.actual))

    public actual fun upper(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.upper(operand.actual))

    public actual fun millisToString(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.millisToString(operand.actual))

    public actual fun millisToUTC(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.millisToUTC(operand.actual))

    public actual fun stringToMillis(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.stringToMillis(operand.actual))

    public actual fun stringToUTC(operand: Expression): Expression =
        DelegatedExpression(CBLFunction.stringToUTC(operand.actual))
}
