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

import kotbase.ext.toStringMillis
import kotbase.util.identityHashCodeHex
import kotlin.time.Instant

public actual open class Expression {

    private class ValueExpression(private val value: Any?) : Expression() {

        init {
            verifySupportedType(value)
        }

        override fun asJSON(): Any? =
            asJSON(value)

        @Suppress("UNCHECKED_CAST")
        private fun asJSON(value: Any?): Any? = when (value) {
            is Instant -> value.toStringMillis()
            is Map<*, *> -> mapAsJSON(value as Map<String, Any?>)
            is List<*> -> listAsJSON(value)
            is Expression -> value.asJSON()
            else -> {
                verifySupportedType(value)
                value
            }
        }

        private fun mapAsJSON(map: Map<String, Any?>): Any =
            map.mapValues { asJSON(it.value) }

        private fun listAsJSON(list: List<Any?>): Any {
            return buildList {
                add("[]") // Array Operation
                list.forEach {
                    add(asJSON(it))
                }
            }
        }

        private fun verifySupportedType(value: Any?) {
            if (value == null
                || value is String
                || value is Number // including int, long, float, double
                || value is Boolean
                || value is Instant
                || value is Map<*, *>
                || value is List<*>
                || value is Expression
            ) {
                return
            }
            throw IllegalArgumentException("Unsupported expression value type: " + value::class)
        }
    }

    private class AggregateExpression(val expressions: List<Expression>) : Expression() {

        override fun asJSON(): Any {
            return buildList {
                add("[]")
                expressions.forEach {
                    add(it.asJSON())
                }
            }
        }
    }

    private class BinaryExpression(
        private val lhs: Expression,
        private val rhs: Expression,
        private val op: String
    ) : Expression() {

        companion object {
            const val OP_ADD = "+"
            const val OP_BETWEEN = "BETWEEN"
            const val OP_DIVIDE = "/"
            const val OP_EQUALS = "="
            const val OP_GREATER = ">"
            const val OP_GREATER_OR_EQUAL = ">="
            const val OP_IN = "IN"
            const val OP_IS = "IS"
            const val OP_IS_NOT = "IS NOT"
            const val OP_LESS = "<"
            const val OP_LESS_OR_EQUAL = "<="
            const val OP_LIKE = "LIKE"
            const val OP_MODULO = "%"
            const val OP_MULTIPLY = "*"
            const val OP_NOT_EQUAL = "!="
            const val OP_SUBTRACT = "-"
            const val OP_REGEX_LIKE = "regexp_like()"
        }

        override fun asJSON(): Any {
            return buildList {
                add(op)
                add(lhs.asJSON())
                if (op != OP_BETWEEN) {
                    add(rhs.asJSON())
                } else {
                    // "between"'s RHS is an aggregate of the min and max, but the min and max need to be
                    // written out as parameters to the BETWEEN operation:
                    val rangeExprs = (rhs as AggregateExpression).expressions
                    add(rangeExprs[0].asJSON())
                    add(rangeExprs[1].asJSON())
                }
            }
        }
    }

    private class CompoundExpression(
        private val subexpressions: List<Expression>,
        private val op: String
    ) : Expression() {

        companion object {
            const val OP_AND = "AND"
            const val OP_OR = "OR"
            const val OP_NOT = "NOT"
        }

        override fun asJSON(): Any {
            return buildList {
                add(op)
                subexpressions.forEach {
                    add(it.asJSON())
                }
            }
        }
    }

    private class UnaryExpression(
        private val operand: Expression,
        private val type: OpType
    ) : Expression() {

        enum class OpType {
            Missing, NotMissing, NotNull, Null, Valued
        }

        override fun asJSON(): Any {
            val opd = operand.asJSON()
            return buildList {
                when (type) {
                    OpType.Valued -> {
                        add("IS VALUED")
                        add(opd)
                    }
                    OpType.Missing -> {
                        add("IS")
                        add(opd)
                        add(listOf("MISSING"))
                    }
                    OpType.NotMissing -> {
                        add("IS NOT")
                        add(opd)
                        add(listOf("MISSING"))
                    }
                    OpType.Null -> {
                        add("IS")
                        add(opd)
                        add(null)
                    }
                    OpType.NotNull -> {
                        add("IS NOT")
                        add(opd)
                        add(null)
                    }
                }
            }
        }
    }

    private class ParameterExpression(val name: String) : Expression() {

        override fun asJSON(): Any =
            listOf("$$name")
    }

    private class CollationExpression(
        private val operand: Expression,
        private val collation: Collation
    ) : Expression() {

        override fun asJSON(): Any {
            return listOf(
                "COLLATE",
                collation.asJSON(),
                operand.asJSON()
            )
        }
    }

    internal class FunctionExpression(
        private val func: String,
        private val params: List<Expression>
    ) : Expression() {

        override fun asJSON(): Any {
            return buildList {
                add(func)
                params.forEach {
                    add(it.asJSON())
                }
            }
        }
    }

    internal class IdxExpression(
        private val func: String,
        private val idx: IndexExpression,
        private vararg val params: Expression
    ) : Expression() {

        override fun asJSON(): Any {
            return buildList {
                add(func)
                add(idx.toString())
                params.forEach {
                    add(it.asJSON())
                }
            }
        }
    }

    public actual companion object {

        public actual fun value(value: Any?): Expression =
            ValueExpression(value)

        public actual fun string(value: String?): Expression =
            ValueExpression(value)

        public actual fun number(value: Number?): Expression =
            ValueExpression(value)

        public actual fun intValue(value: Int): Expression =
            ValueExpression(value)

        public actual fun longValue(value: Long): Expression =
            ValueExpression(value)

        public actual fun floatValue(value: Float): Expression =
            ValueExpression(value)

        public actual fun doubleValue(value: Double): Expression =
            ValueExpression(value)

        public actual fun booleanValue(value: Boolean): Expression =
            ValueExpression(value)

        public actual fun date(value: Instant?): Expression =
            ValueExpression(value)

        public actual fun map(value: Map<String, Any?>?): Expression =
            ValueExpression(value)

        public actual fun list(value: List<Any?>?): Expression =
            ValueExpression(value)

        public actual fun all(): PropertyExpression =
            PropertyExpression("") // CBLPropertyExpression.kCBLAllPropertiesName = ""

        public actual fun property(property: String): PropertyExpression =
            PropertyExpression(property)

        public actual fun parameter(name: String): Expression =
            ParameterExpression(name)

        public actual fun negated(expression: Expression): Expression =
            CompoundExpression(listOf(expression), CompoundExpression.OP_NOT)

        public actual fun not(expression: Expression): Expression =
            negated(expression)

        public actual fun fullTextIndex(indexName: String): FullTextIndexExpression =
            FTIExpression(indexName)

        private class FTIExpression(
            private val name: String,
            private val alias: String? = null
        ) : FullTextIndexExpression {

            override fun from(alias: String): IndexExpression =
                FTIExpression(name, alias)

            override fun toString(): String {
                return buildString {
                    if (alias != null) {
                        append(alias).append('.')
                    }
                    append(name)
                }
            }
        }
    }

    public actual infix fun multiply(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OP_MULTIPLY)

    public actual infix fun divide(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OP_DIVIDE)

    public actual infix fun modulo(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OP_MODULO)

    public actual infix fun add(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OP_ADD)

    public actual infix fun subtract(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OP_SUBTRACT)

    public actual infix fun lessThan(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OP_LESS)

    public actual infix fun lessThanOrEqualTo(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OP_LESS_OR_EQUAL)

    public actual infix fun greaterThan(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OP_GREATER)

    public actual infix fun greaterThanOrEqualTo(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OP_GREATER_OR_EQUAL)

    public actual infix fun equalTo(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OP_EQUALS)

    public actual infix fun notEqualTo(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OP_NOT_EQUAL)

    public actual infix fun and(expression: Expression): Expression =
        CompoundExpression(listOf(this, expression), CompoundExpression.OP_AND)

    public actual infix fun or(expression: Expression): Expression =
        CompoundExpression(listOf(this, expression), CompoundExpression.OP_OR)

    public actual infix fun like(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OP_LIKE)

    public actual infix fun regex(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OP_REGEX_LIKE)

    public actual infix fun `is`(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OP_IS)

    public actual infix fun isNot(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OP_IS_NOT)

    public actual fun between(expression1: Expression, expression2: Expression): Expression {
        val aggr = AggregateExpression(listOf(expression1, expression2))
        return BinaryExpression(this, aggr, BinaryExpression.OP_BETWEEN)
    }

    public actual infix fun collate(collation: Collation): Expression =
        CollationExpression(this, collation)

    public actual fun `in`(vararg expressions: Expression): Expression {
        require(expressions.isNotEmpty()) { "empty 'IN'." }
        val aggr = AggregateExpression(expressions.asList())
        return BinaryExpression(this, aggr, BinaryExpression.OP_IN)
    }

    public actual fun isValued(): Expression =
        UnaryExpression(this, UnaryExpression.OpType.Valued)

    public actual fun isNotValued(): Expression =
        negated(isValued())

    override fun toString(): String =
        "${this::class.simpleName} {@${identityHashCodeHex()},json=" + asJSON() + "}"

    internal open fun asJSON(): Any? {
        throw IllegalStateException("Should be overridden in subclass ${this::class}")
    }
}
