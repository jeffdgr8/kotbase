package kotbase

import com.couchbase.lite.Function as CBLFunction

public actual object Function {

    public actual fun avg(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.avg(operand.actual))

    public actual fun count(operand: Expression?): Expression =
        ExpressionImpl(CBLFunction.count(operand?.actual))

    public actual fun min(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.min(operand.actual))

    public actual fun max(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.max(operand.actual))

    public actual fun sum(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.sum(operand.actual))

    public actual fun abs(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.abs(operand.actual))

    public actual fun acos(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.acos(operand.actual))

    public actual fun asin(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.asin(operand.actual))

    public actual fun atan(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.atan(operand.actual))

    public actual fun atan2(y: Expression, x: Expression): Expression =
        ExpressionImpl(CBLFunction.atan2(y.actual, x.actual))

    public actual fun ceil(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.ceil(operand.actual))

    public actual fun cos(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.cos(operand.actual))

    public actual fun degrees(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.degrees(operand.actual))

    public actual fun e(): Expression =
        ExpressionImpl(CBLFunction.e())

    public actual fun exp(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.exp(operand.actual))

    public actual fun floor(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.floor(operand.actual))

    public actual fun ln(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.ln(operand.actual))

    public actual fun log(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.log(operand.actual))

    public actual fun pi(): Expression =
        ExpressionImpl(CBLFunction.pi())

    public actual fun power(base: Expression, exp: Expression): Expression =
        ExpressionImpl(CBLFunction.power(base.actual, exp.actual))

    public actual fun radians(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.radians(operand.actual))

    public actual fun round(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.round(operand.actual))

    public actual fun round(operand: Expression, digits: Expression): Expression =
        ExpressionImpl(CBLFunction.round(operand.actual, digits.actual))

    public actual fun sign(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.sign(operand.actual))

    public actual fun sin(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.sin(operand.actual))

    public actual fun sqrt(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.sqrt(operand.actual))

    public actual fun tan(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.tan(operand.actual))

    public actual fun trunc(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.trunc(operand.actual))

    public actual fun trunc(operand: Expression, digits: Expression): Expression =
        ExpressionImpl(CBLFunction.trunc(operand.actual, digits.actual))

    public actual fun contains(operand: Expression, substring: Expression): Expression =
        ExpressionImpl(CBLFunction.contains(operand.actual, substring.actual))

    public actual fun length(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.length(operand.actual))

    public actual fun lower(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.lower(operand.actual))

    public actual fun ltrim(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.ltrim(operand.actual))

    public actual fun rtrim(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.rtrim(operand.actual))

    public actual fun trim(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.trim(operand.actual))

    public actual fun upper(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.upper(operand.actual))

    public actual fun millisToString(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.millisToString(operand.actual))

    public actual fun millisToUTC(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.millisToUTC(operand.actual))

    public actual fun stringToMillis(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.stringToMillis(operand.actual))

    public actual fun stringToUTC(operand: Expression): Expression =
        ExpressionImpl(CBLFunction.stringToUTC(operand.actual))
}
