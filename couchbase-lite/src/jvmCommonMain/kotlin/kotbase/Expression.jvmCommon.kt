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

import kotbase.ext.toDate
import kotbase.internal.DelegatedClass
import kotbase.internal.actuals
import kotlinx.datetime.Instant
import com.couchbase.lite.Expression as CBLExpression
import com.couchbase.lite.FullTextIndexExpression as CBLFullTextIndexExpression
import com.couchbase.lite.IndexExpression as CBLIndexExpression

public actual open class Expression(actual: CBLExpression) : DelegatedClass<CBLExpression>(actual) {

    public actual companion object {

        public actual fun value(value: Any?): Expression =
            Expression(CBLExpression.value(value?.actualIfDelegated()))

        public actual fun string(value: String?): Expression =
            Expression(CBLExpression.string(value))

        public actual fun number(value: Number?): Expression =
            Expression(CBLExpression.number(value))

        public actual fun intValue(value: Int): Expression =
            Expression(CBLExpression.intValue(value))

        public actual fun longValue(value: Long): Expression =
            Expression(CBLExpression.longValue(value))

        public actual fun floatValue(value: Float): Expression =
            Expression(CBLExpression.floatValue(value))

        public actual fun doubleValue(value: Double): Expression =
            Expression(CBLExpression.doubleValue(value))

        public actual fun booleanValue(value: Boolean): Expression =
            Expression(CBLExpression.booleanValue(value))

        public actual fun date(value: Instant?): Expression =
            Expression(CBLExpression.date(value?.toDate()))

        public actual fun map(value: Map<String, Any?>?): Expression =
            Expression(CBLExpression.map(value?.actualIfDelegated()))

        public actual fun list(value: List<Any?>?): Expression =
            Expression(CBLExpression.list(value?.actualIfDelegated()))

        public actual fun all(): PropertyExpression =
            PropertyExpression(CBLExpression.all())

        public actual fun property(property: String): PropertyExpression =
            PropertyExpression(CBLExpression.property(property))

        public actual fun parameter(name: String): Expression =
            Expression(CBLExpression.parameter(name))

        public actual fun negated(expression: Expression): Expression =
            Expression(CBLExpression.negated(expression.actual))

        public actual fun not(expression: Expression): Expression =
            Expression(CBLExpression.not(expression.actual))

        public actual fun fullTextIndex(indexName: String): FullTextIndexExpression =
            DelegatedFullTextIndexExpression(CBLExpression.fullTextIndex(indexName))

        private class DelegatedFullTextIndexExpression(
            actual: CBLFullTextIndexExpression
        ) : DelegatedIndexExpression<CBLFullTextIndexExpression>(actual), FullTextIndexExpression {

            override fun from(alias: String): IndexExpression =
                DelegatedIndexExpression(actual.from(alias))
        }

        internal open class DelegatedIndexExpression<A : CBLIndexExpression>(
            actual: A
        ) : DelegatedClass<A>(actual), IndexExpression
    }

    public actual infix fun multiply(expression: Expression): Expression =
        Expression(actual.multiply(expression.actual))

    public actual infix fun divide(expression: Expression): Expression =
        Expression(actual.divide(expression.actual))

    public actual infix fun modulo(expression: Expression): Expression =
        Expression(actual.modulo(expression.actual))

    public actual infix fun add(expression: Expression): Expression =
        Expression(actual.add(expression.actual))

    public actual infix fun subtract(expression: Expression): Expression =
        Expression(actual.subtract(expression.actual))

    public actual infix fun lessThan(expression: Expression): Expression =
        Expression(actual.lessThan(expression.actual))

    public actual infix fun lessThanOrEqualTo(expression: Expression): Expression =
        Expression(actual.lessThanOrEqualTo(expression.actual))

    public actual infix fun greaterThan(expression: Expression): Expression =
        Expression(actual.greaterThan(expression.actual))

    public actual infix fun greaterThanOrEqualTo(expression: Expression): Expression =
        Expression(actual.greaterThanOrEqualTo(expression.actual))

    public actual infix fun equalTo(expression: Expression): Expression =
        Expression(actual.equalTo(expression.actual))

    public actual infix fun notEqualTo(expression: Expression): Expression =
        Expression(actual.notEqualTo(expression.actual))

    public actual infix fun and(expression: Expression): Expression =
        Expression(actual.and(expression.actual))

    public actual infix fun or(expression: Expression): Expression =
        Expression(actual.or(expression.actual))

    public actual infix fun like(expression: Expression): Expression =
        Expression(actual.like(expression.actual))

    public actual infix fun regex(expression: Expression): Expression =
        Expression(actual.regex(expression.actual))

    public actual infix fun `is`(expression: Expression): Expression =
        Expression(actual.`is`(expression.actual))

    public actual infix fun isNot(expression: Expression): Expression =
        Expression(actual.isNot(expression.actual))

    public actual fun between(expression1: Expression, expression2: Expression): Expression =
        Expression(actual.between(expression1.actual, expression2.actual))

    public actual infix fun collate(collation: Collation): Expression =
        Expression(actual.collate(collation.actual))

    public actual fun `in`(vararg expressions: Expression): Expression =
        Expression(actual.`in`(*expressions.actuals()))

    public actual fun isValued(): Expression =
        Expression(actual.isValued)

    public actual fun isNotValued(): Expression =
        Expression(actual.isNotValued)
}
