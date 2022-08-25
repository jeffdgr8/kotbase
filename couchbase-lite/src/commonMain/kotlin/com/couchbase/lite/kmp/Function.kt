@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite.kmp

/**
 * Query functions.
 */
public expect object Function {

    /**
     * Creates an AVG(expr) function expression that returns the average of all the number values
     * in the group of the values expressed by the given expression.
     *
     * @param operand The expression.
     * @return The AVG(expr) function.
     */
    public fun avg(operand: Expression): Expression

    /**
     * Creates a COUNT(expr) function expression that returns the count of all values
     * in the group of the values expressed by the given expression.
     * Null expression is count *
     *
     * @param operand The expression.
     * @return The COUNT(expr) function.
     */
    public fun count(operand: Expression?): Expression

    /**
     * Creates a MIN(expr) function expression that returns the minimum value
     * in the group of the values expressed by the given expression.
     *
     * @param operand The expression.
     * @return The MIN(expr) function.
     */
    public fun min(operand: Expression): Expression

    /**
     * Creates a MAX(expr) function expression that returns the maximum value
     * in the group of the values expressed by the given expression.
     *
     * @param operand The expression.
     * @return The MAX(expr) function.
     */
    public fun max(operand: Expression): Expression

    /**
     * Creates a SUM(expr) function expression that return the sum of all number values
     * in the group of the values expressed by the given expression.
     *
     * @param operand The expression.
     * @return The SUM(expr) function.
     */
    public fun sum(operand: Expression): Expression

    /**
     * Creates an ABS(expr) function that returns the absolute value of the given numeric
     * expression.
     *
     * @param operand The expression.
     * @return The ABS(expr) function.
     */
    public fun abs(operand: Expression): Expression

    /**
     * Creates an ACOS(expr) function that returns the inverse cosine of the given numeric
     * expression.
     *
     * @param operand The expression.
     * @return The ACOS(expr) function.
     */
    public fun acos(operand: Expression): Expression

    /**
     * Creates an ASIN(expr) function that returns the inverse sin of the given numeric
     * expression.
     *
     * @param operand The expression.
     * @return The ASIN(expr) function.
     */
    public fun asin(operand: Expression): Expression

    /**
     * Creates an ATAN(expr) function that returns the inverse tangent of the numeric
     * expression.
     *
     * @param operand The expression.
     * @return The ATAN(expr) function.
     */
    public fun atan(operand: Expression): Expression

    /**
     * Returns the angle theta from the conversion of rectangular coordinates (x, y)
     * to polar coordinates (r, theta).
     *
     * @param y the ordinate coordinate
     * @param x the abscissa coordinate
     * @return the theta component of the point (r, theta) in polar coordinates that corresponds
     * to the point (x, y) in Cartesian coordinates.
     */
    public fun atan2(y: Expression, x: Expression): Expression

    /**
     * Creates a CEIL(expr) function that returns the ceiling value of the given numeric
     * expression.
     *
     * @param operand The expression.
     * @return The CEIL(expr) function.
     */
    public fun ceil(operand: Expression): Expression

    /**
     * Creates a COS(expr) function that returns the cosine of the given numeric expression.
     *
     * @param operand The expression.
     * @return The COS(expr) function.
     */
    public fun cos(operand: Expression): Expression

    /**
     * Creates a DEGREES(expr) function that returns the degrees value of the given radiants
     * value expression.
     *
     * @param operand The expression.
     * @return The DEGREES(expr) function.
     */
    public fun degrees(operand: Expression): Expression

    /**
     * Creates a E() function that return the value of the mathematical constant 'e'.
     *
     * @return The E() constant function.
     */
    public fun e(): Expression

    /**
     * Creates a EXP(expr) function that returns the value of 'e' power by the given numeric
     * expression.
     *
     * @param operand The expression.
     * @return The EXP(expr) function.
     */
    public fun exp(operand: Expression): Expression

    /**
     * Creates a FLOOR(expr) function that returns the floor value of the given
     * numeric expression.
     *
     * @param operand The expression.
     * @return The FLOOR(expr) function.
     */
    public fun floor(operand: Expression): Expression

    /**
     * Creates a LN(expr) function that returns the natural log of the given numeric expression.
     *
     * @param operand The expression.
     * @return The LN(expr) function.
     */
    public fun ln(operand: Expression): Expression

    /**
     * Creates a LOG(expr) function that returns the base 10 log of the given numeric expression.
     *
     * @param operand The expression.
     * @return The LOG(expr) function.
     */
    public fun log(operand: Expression): Expression

    /**
     * Creates a PI() function that returns the mathematical constant Pi.
     *
     * @return The PI() constant function.
     */
    public fun pi(): Expression

    /**
     * Creates a POWER(base, exponent) function that returns the value of the given base
     * expression power the given exponent expression.
     *
     * @param base The base expression.
     * @param exp  The exponent expression.
     * @return The POWER(base, exponent) function.
     */
    public fun power(base: Expression, exp: Expression): Expression

    /**
     * Creates a RADIANS(expr) function that returns the radians value of the given degrees
     * value expression.
     *
     * @param operand The expression.
     * @return The RADIANS(expr) function.
     */
    public fun radians(operand: Expression): Expression

    /**
     * Creates a ROUND(expr) function that returns the rounded value of the given numeric
     * expression.
     *
     * @param operand The expression.
     * @return The ROUND(expr) function.
     */
    public fun round(operand: Expression): Expression

    /**
     * Creates a ROUND(expr, digits) function that returns the rounded value to the given
     * number of digits of the given numeric expression.
     *
     * @param operand The numeric expression.
     * @param digits  The number of digits.
     * @return The ROUND(expr, digits) function.
     */
    public fun round(operand: Expression, digits: Expression): Expression

    /**
     * Creates a SIGN(expr) function that returns the sign (1: positive, -1: negative, 0: zero)
     * of the given numeric expression.
     *
     * @param operand The expression.
     * @return The SIGN(expr) function.
     */
    public fun sign(operand: Expression): Expression

    /**
     * Creates a SIN(expr) function that returns the sin of the given numeric expression.
     *
     * @param operand The numeric expression.
     * @return The SIN(expr) function.
     */
    public fun sin(operand: Expression): Expression

    /**
     * Creates a SQRT(expr) function that returns the square root of the given numeric expression.
     *
     * @param operand The numeric expression.
     * @return The SQRT(expr) function.
     */
    public fun sqrt(operand: Expression): Expression

    /**
     * Creates a TAN(expr) function that returns the tangent of the given numeric expression.
     *
     * @param operand The numeric expression.
     * @return The TAN(expr) function.
     */
    public fun tan(operand: Expression): Expression

    /**
     * Creates a TRUNC(expr) function that truncates all of the digits after the decimal place
     * of the given numeric expression.
     *
     * @param operand The numeric expression.
     * @return The trunc function.
     */
    public fun trunc(operand: Expression): Expression

    /**
     * Creates a TRUNC(expr, digits) function that truncates the number of the digits after
     * the decimal place of the given numeric expression.
     *
     * @param operand The numeric expression.
     * @param digits  The number of digits to truncate.
     * @return The TRUNC(expr, digits) function.
     */
    public fun trunc(operand: Expression, digits: Expression): Expression

    /**
     * Creates a CONTAINS(expr, substr) function that evaluates whether the given string
     * expression conatins the given substring expression or not.
     *
     * @param operand   The string expression.
     * @param substring The substring expression.
     * @return The CONTAINS(expr, substr) function.
     */
    public fun contains(operand: Expression, substring: Expression): Expression

    /**
     * Creates a LENGTH(expr) function that returns the length of the given string expression.
     *
     * @param operand The string expression.
     * @return The LENGTH(expr) function.
     */
    public fun length(operand: Expression): Expression

    /**
     * Creates a LOWER(expr) function that returns the lowercase string of the given string
     * expression.
     *
     * @param operand The string expression.
     * @return The LOWER(expr) function.
     */
    public fun lower(operand: Expression): Expression

    /**
     * Creates a LTRIM(expr) function that removes the whitespace from the beginning of the
     * given string expression.
     *
     * @param operand The string expression.
     * @return The LTRIM(expr) function.
     */
    public fun ltrim(operand: Expression): Expression

    /**
     * Creates a RTRIM(expr) function that removes the whitespace from the end of the
     * given string expression.
     *
     * @param operand The string expression.
     * @return The RTRIM(expr) function.
     */
    public fun rtrim(operand: Expression): Expression

    /**
     * Creates a TRIM(expr) function that removes the whitespace from the beginning and
     * the end of the given string expression.
     *
     * @param operand The string expression.
     * @return The TRIM(expr) function.
     */
    public fun trim(operand: Expression): Expression

    /**
     * Creates a UPPER(expr) function that returns the uppercase string of the given string expression.
     *
     * @param operand The string expression.
     * @return The UPPER(expr) function.
     */
    public fun upper(operand: Expression): Expression

    /**
     * Creates a MILLIS_TO_STR(expr) function that will convert a numeric input representing
     * milliseconds since the Unix epoch into a full ISO8601 date and time
     * string in the device local time zone.
     *
     * @param operand The string expression.
     * @return The MILLIS_TO_STR(expr) function.
     */
    public fun millisToString(operand: Expression): Expression

    /**
     * Creates a MILLIS_TO_UTC(expr) function that will convert a numeric input representing
     * milliseconds since the Unix epoch into a full ISO8601 date and time
     * string in UTC time.
     *
     * @param operand The string expression.
     * @return The MILLIS_TO_UTC(expr) function.
     */
    public fun millisToUTC(operand: Expression): Expression

    /**
     * Creates a STR_TO_MILLIS(expr) that will convert an ISO8601 datetime string
     * into the number of milliseconds since the unix epoch.
     * Valid date strings must start with a date in the form YYYY-MM-DD (time
     * only strings are not supported).
     *
     * Times can be of the form HH:MM, HH:MM:SS, or HH:MM:SS.FFF.  Leading zero is
     * not optional (i.e. 02 is ok, 2 is not).  Hours are in 24-hour format.  FFF
     * represents milliseconds, and *trailing* zeros are optional (i.e. 5 == 500).
     *
     * Time zones can be in one of three forms:
     * (+/-)HH:MM
     * (+/-)HHMM
     * Z (which represents UTC)
     *
     * @param operand The string expression.
     * @return The STR_TO_MILLIS(expr) function.
     */
    public fun stringToMillis(operand: Expression): Expression

    /**
     * Creates a STR_TO_UTC(expr) that will convert an ISO8601 datetime string
     * into a full ISO8601 UTC datetime string.
     * Valid date strings must start with a date in the form YYYY-MM-DD (time
     * only strings are not supported).
     *
     * Times can be of the form HH:MM, HH:MM:SS, or HH:MM:SS.FFF.  Leading zero is
     * not optional (i.e. 02 is ok, 2 is not).  Hours are in 24-hour format.  FFF
     * represents milliseconds, and *trailing* zeros are optional (i.e. 5 == 500).
     *
     * Time zones can be in one of three forms:
     * (+/-)HH:MM
     * (+/-)HHMM
     * Z (which represents UTC)
     *
     * @param operand The string expression.
     * @return The STR_TO_UTC(expr) function.
     */
    public fun stringToUTC(operand: Expression): Expression
}
