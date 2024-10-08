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

import kotlinx.datetime.Instant

/**
 * The expression used in constructing a query.
 */
public expect open class Expression {

    public companion object {

        /**
         * Create value expression with given value
         *
         * @param value the value
         * @return the value expression
         */
        public fun value(value: Any?): Expression

        /**
         * Create value expression with given String value
         *
         * @param value the String value
         * @return the value expression
         */
        public fun string(value: String?): Expression

        /**
         * Create value expression with given Number value
         *
         * @param value the Number value
         * @return the value expression
         */
        public fun number(value: Number?): Expression

        /**
         * Create value expression with given integer value
         *
         * @param value the integer value
         * @return the value expression
         */
        public fun intValue(value: Int): Expression

        /**
         * Create value expression with given long value
         *
         * @param value the long value
         * @return the value expression
         */
        public fun longValue(value: Long): Expression

        /**
         * Create value expression with given float value
         *
         * @param value the float value
         * @return the value expression
         */
        public fun floatValue(value: Float): Expression

        /**
         * Create value expression with given double value
         *
         * @param value the double value
         * @return the value expression
         */
        public fun doubleValue(value: Double): Expression

        /**
         * Create value expression with given boolean value
         *
         * @param value the boolean value
         * @return the value expression
         */
        public fun booleanValue(value: Boolean): Expression

        /**
         * Create value expression with given Date value
         *
         * @param value the Date value
         * @return the value expression
         */
        public fun date(value: Instant?): Expression

        /**
         * Creates value expression with the given map.
         *
         * @param value the map value
         * @return the value expression.
         */
        public fun map(value: Map<String, Any?>?): Expression

        /**
         * Create value expression with the given list.
         *
         * @param value the list value.
         * @return the value expression.
         */
        public fun list(value: List<Any?>?): Expression

        /**
         * Creates a * expression to express all properties
         *
         * @return a property expression.
         */
        public fun all(): PropertyExpression

        /**
         * Create a property expression representing the value of the given property.
         *
         * @param property the name of the property in the form of a key path.
         * @return a property expression.
         */
        public fun property(property: String): PropertyExpression

        /**
         * Creates a parameter expression with the given parameter name.
         *
         * @param name The parameter name
         * @return A parameter expression.
         */
        public fun parameter(name: String): Expression

        /**
         * Create a negated expression to represent the negated result of the given expression.
         *
         * @param expression the expression to be negated.
         * @return a negated expression.
         */
        public fun negated(expression: Expression): Expression

        /**
         * Create a negated expression to represent the negated result of the given expression.
         *
         * @param expression the expression to be negated.
         * @return a negated expression.
         */
        public fun not(expression: Expression): Expression

        /**
         * Create a full-text index expression referencing a full-text index with the given index name.
         *
         * When there is a need to specify the data source in which the index has been created (e.g. in
         * multi-collection join statement, calls the from(_ alias: String) method from the returned
         * FullTextIndexExpressionProtocol object to specify the data source.
         *
         * @param indexName The name of the full-text index.
         * @return The full-text index expression referring to a full text index in the specified data source.
         */
        public fun fullTextIndex(indexName: String): FullTextIndexExpression
    }

    /**
     * Create a multiply expression to multiply the current expression by the given expression.
     *
     * @param expression the expression to multiply by.
     * @return a multiply expression.
     */
    public infix fun multiply(expression: Expression): Expression

    /**
     * Create a divide expression to divide the current expression by the given expression.
     *
     * @param expression the expression to divide by.
     * @return a divide expression.
     */
    public infix fun divide(expression: Expression): Expression

    /**
     * Create a modulo expression to modulo the current expression by the given expression.
     *
     * @param expression the expression to modulo by.
     * @return a modulo expression.
     */
    public infix fun modulo(expression: Expression): Expression

    /**
     * Create an add expression to add the given expression to the current expression
     *
     * @param expression an expression to add to the current expression.
     * @return an add expression.
     */
    public infix fun add(expression: Expression): Expression

    /**
     * Create a subtract expression to subtract the given expression from the current expression.
     *
     * @param expression an expression to subtract from the current expression.
     * @return a subtract expression.
     */
    public infix fun subtract(expression: Expression): Expression

    /**
     * Create a less than expression that evaluates whether or not the current expression
     * is less than the given expression.
     *
     * @param expression the expression to compare with the current expression.
     * @return a less than expression.
     */
    public infix fun lessThan(expression: Expression): Expression

    /**
     * Create a less than or equal to expression that evaluates whether or not the current
     * expression is less than or equal to the given expression.
     *
     * @param expression the expression to compare with the current expression.
     * @return a less than or equal to expression.
     */
    public infix fun lessThanOrEqualTo(expression: Expression): Expression

    /**
     * Create a greater than expression that evaluates whether or not the current expression
     * is greater than the given expression.
     *
     * @param expression the expression to compare with the current expression.
     * @return a greater than expression.
     */
    public infix fun greaterThan(expression: Expression): Expression

    /**
     * Create a greater than or equal to expression that evaluates whether or not the current
     * expression is greater than or equal to the given expression.
     *
     * @param expression the expression to compare with the current expression.
     * @return a greater than or equal to expression.
     */
    public infix fun greaterThanOrEqualTo(expression: Expression): Expression

    /**
     * Create an equal to expression that evaluates whether or not the current expression
     * is equal to the given expression.
     *
     * @param expression the expression to compare with the current expression.
     * @return an equal to expression.
     */
    public infix fun equalTo(expression: Expression): Expression

    /**
     * Create a NOT equal to expression that evaluates whether or not the current expression
     * is not equal to the given expression.
     *
     * @param expression the expression to compare with the current expression.
     * @return a NOT equal to expression.
     */
    public infix fun notEqualTo(expression: Expression): Expression

    /**
     * Create a logical AND expression that performs logical AND operation with
     * the current expression.
     *
     * @param expression the expression to AND with the current expression.
     * @return a logical AND expression.
     */
    public infix fun and(expression: Expression): Expression

    /**
     * Create a logical OR expression that performs logical OR operation with
     * the current expression.
     *
     * @param expression the expression to OR with the current expression.
     * @return a logical OR expression.
     */
    public infix fun or(expression: Expression): Expression

    /**
     * Create a Like expression that evaluates whether or not the current expression is LIKE
     * the given expression.
     *
     * @param expression the expression to compare with the current expression.
     * @return a Like expression.
     */
    public infix fun like(expression: Expression): Expression

    /**
     * Create a regex match expression that evaluates whether or not the current expression
     * regex matches the given expression.
     *
     * @param expression the expression to compare with the current expression.
     * @return a regex match expression.
     */
    public infix fun regex(expression: Expression): Expression

    /**
     * Create an IS expression that evaluates whether or not the current expression is equal to
     * the given expression.
     *
     * @param expression the expression to compare with the current expression.
     * @return an IS expression.
     */
    public infix fun `is`(expression: Expression): Expression

    /**
     * Create an IS NOT expression that evaluates whether or not the current expression is not
     * equal to the given expression.
     *
     * @param expression the expression to compare with the current expression.
     * @return an IS NOT expression.
     */
    public infix fun isNot(expression: Expression): Expression

    /**
     * Create a between expression that evaluates whether or not the current expression is
     * between the given expressions inclusively.
     *
     * @param expression1 the inclusive lower bound expression.
     * @param expression2 the inclusive upper bound expression.
     * @return a between expression.
     */
    public fun between(expression1: Expression, expression2: Expression): Expression

    /**
     * Creates a Collate expression with the given Collation specification. Commonly
     * the collate expression is used in the Order BY clause or the string comparison
     * 　expression (e.g. equalTo or lessThan) to specify how the two strings are　compared.
     *
     * @param collation 　The collation object.
     * @return A Collate expression.
     */
    public infix fun collate(collation: Collation): Expression

    /**
     * Create an IN expression that evaluates whether or not the current expression is in the
     * given expressions.
     *
     * @param expressions the expression array to evaluate with.
     * @return an IN expression.
     */
    public fun `in`(vararg expressions: Expression): Expression

    /**
     * Creates an IS VALUED expression that returns true if the current
     * expression is valued.
     *
     * @return An IS VALUED expression.
     */
    public fun isValued(): Expression

    /**
     * Creates an NOT IS VALUED expression that returns true if the current
     * expression is NOT VALUED.
     *
     * @return An IS NOT VALUED expression.
     */
    public fun isNotValued(): Expression
}
