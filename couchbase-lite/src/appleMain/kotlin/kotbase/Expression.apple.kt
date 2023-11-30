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

import cocoapods.CouchbaseLite.CBLQueryExpression
import cocoapods.CouchbaseLite.CBLQueryFullTextIndexExpressionProtocolProtocol
import cocoapods.CouchbaseLite.CBLQueryIndexExpressionProtocolProtocol
import kotbase.internal.DelegatedClass
import kotbase.internal.DelegatedProtocol
import kotbase.internal.actuals
import kotlinx.cinterop.convert
import kotlinx.datetime.Instant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSNumber

public actual open class Expression
internal constructor(actual: CBLQueryExpression) : DelegatedClass<CBLQueryExpression>(actual) {

    public actual companion object {

        public actual fun value(value: Any?): Expression =
            Expression(CBLQueryExpression.value(value?.actualIfDelegated()))

        public actual fun string(value: String?): Expression =
            Expression(CBLQueryExpression.string(value))

        public actual fun number(value: Number?): Expression =
            Expression(CBLQueryExpression.number(value as NSNumber?))

        public actual fun intValue(value: Int): Expression =
            Expression(CBLQueryExpression.integer(value.convert()))

        public actual fun longValue(value: Long): Expression =
            Expression(CBLQueryExpression.longLong(value))

        public actual fun floatValue(value: Float): Expression =
            Expression(CBLQueryExpression.float(value))

        public actual fun doubleValue(value: Double): Expression =
            Expression(CBLQueryExpression.double(value))

        public actual fun booleanValue(value: Boolean): Expression =
            Expression(CBLQueryExpression.boolean(value))

        public actual fun date(value: Instant?): Expression =
            Expression(CBLQueryExpression.date(value?.toNSDate()))

        public actual fun map(value: Map<String, Any?>?): Expression =
            Expression(CBLQueryExpression.dictionary(value?.actualIfDelegated()))

        public actual fun list(value: List<Any?>?): Expression =
            Expression(CBLQueryExpression.array(value?.actualIfDelegated()))

        public actual fun all(): PropertyExpression =
            PropertyExpression("") // CBLPropertyExpression.kCBLAllPropertiesName = ""

        public actual fun property(property: String): PropertyExpression =
            PropertyExpression(property)

        public actual fun parameter(name: String): Expression =
            Expression(CBLQueryExpression.parameterNamed(name))

        public actual fun negated(expression: Expression): Expression =
            Expression(CBLQueryExpression.negated(expression.actual))

        public actual fun not(expression: Expression): Expression =
            Expression(CBLQueryExpression.not(expression.actual))

        public actual fun fullTextIndex(indexName: String): FullTextIndexExpression =
            DelegatedFullTextIndexExpression(CBLQueryExpression.fullTextIndex(indexName))

        private class DelegatedFullTextIndexExpression(
            actual: CBLQueryFullTextIndexExpressionProtocolProtocol
        ) : DelegatedProtocol<CBLQueryFullTextIndexExpressionProtocolProtocol>(actual), FullTextIndexExpression {

            override fun from(alias: String): IndexExpression =
                DelegatedIndexExpression(actual.from(alias))
        }

        internal class DelegatedIndexExpression<A : CBLQueryIndexExpressionProtocolProtocol>(
            actual: A
        ) : DelegatedProtocol<A>(actual), IndexExpression
    }

    public actual fun multiply(expression: Expression): Expression =
        Expression(actual.multiply(expression.actual))

    public actual fun divide(expression: Expression): Expression =
        Expression(actual.divide(expression.actual))

    public actual fun modulo(expression: Expression): Expression =
        Expression(actual.modulo(expression.actual))

    public actual fun add(expression: Expression): Expression =
        Expression(actual.add(expression.actual))

    public actual fun subtract(expression: Expression): Expression =
        Expression(actual.subtract(expression.actual))

    public actual fun lessThan(expression: Expression): Expression =
        Expression(actual.lessThan(expression.actual))

    public actual fun lessThanOrEqualTo(expression: Expression): Expression =
        Expression(actual.lessThanOrEqualTo(expression.actual))

    public actual fun greaterThan(expression: Expression): Expression =
        Expression(actual.greaterThan(expression.actual))

    public actual fun greaterThanOrEqualTo(expression: Expression): Expression =
        Expression(actual.greaterThanOrEqualTo(expression.actual))

    public actual fun equalTo(expression: Expression): Expression =
        Expression(actual.equalTo(expression.actual))

    public actual fun notEqualTo(expression: Expression): Expression =
        Expression(actual.notEqualTo(expression.actual))

    public actual fun and(expression: Expression): Expression =
        Expression(actual.andExpression(expression.actual))

    public actual fun or(expression: Expression): Expression =
        Expression(actual.orExpression(expression.actual))

    public actual fun like(expression: Expression): Expression =
        Expression(actual.like(expression.actual))

    public actual fun regex(expression: Expression): Expression =
        Expression(actual.regex(expression.actual))

    public actual fun `is`(expression: Expression): Expression =
        Expression(actual.`is`(expression.actual))

    public actual fun isNot(expression: Expression): Expression =
        Expression(actual.isNot(expression.actual))

    public actual fun between(expression1: Expression, expression2: Expression): Expression =
        Expression(actual.between(expression1.actual, expression2.actual))

    public actual fun collate(collation: Collation): Expression =
        Expression(actual.collate(collation.actual))

    public actual fun `in`(vararg expressions: Expression): Expression =
        Expression(actual.`in`(expressions.actuals()))

    public actual fun isValued(): Expression =
        Expression(actual.isValued())

    public actual fun isNotValued(): Expression =
        Expression(actual.isNotValued())
}
