package kotbase

import kotbase.ext.toStringMillis
import kotlinx.datetime.Instant

public actual open class Expression {

    internal class ValueExpression(private val value: Any?) : Expression() {

        init {
            verifySupportedType(value)
        }

        public override fun asJSON(): Any? =
            asJSON(value)

        private fun asJSON(value: Any?): Any? {
            @Suppress("UNCHECKED_CAST")
            return when (value) {
                is Instant -> value.toStringMillis()
                is Map<*, *> -> mapAsJSON(value as Map<String, Any?>)
                is List<*> -> listAsJSON(value)
                is Expression -> value.asJSON()
                else -> {
                    verifySupportedType(value)
                    value
                }
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

    internal class AggregateExpression(val expressions: List<Expression>) : Expression() {

        public override fun asJSON(): Any {
            return buildList {
                add("[]")
                expressions.forEach {
                    add(it.asJSON())
                }
            }
        }
    }

    internal class BinaryExpression(
        private val lhs: Expression,
        private val rhs: Expression,
        private val type: OpType
    ) : Expression() {

        internal enum class OpType {
            Add,
            Between,
            Divide,
            EqualTo,
            GreaterThan,
            GreaterThanOrEqualTo,
            In,
            Is,
            IsNot,
            LessThan,
            LessThanOrEqualTo,
            Like,
            Modulus,
            Multiply,
            NotEqualTo,
            Subtract,
            RegexLike
        }

        public override fun asJSON(): Any {
            return buildList {
                when (type) {
                    OpType.Add -> add("+")
                    OpType.Between -> add("BETWEEN")
                    OpType.Divide -> add("/")
                    OpType.EqualTo -> add("=")
                    OpType.GreaterThan -> add(">")
                    OpType.GreaterThanOrEqualTo -> add(">=")
                    OpType.In -> add("IN")
                    OpType.Is -> add("IS")
                    OpType.IsNot -> add("IS NOT")
                    OpType.LessThan -> add("<")
                    OpType.LessThanOrEqualTo -> add("<=")
                    OpType.Like -> add("LIKE")
                    OpType.Modulus -> add("%")
                    OpType.Multiply -> add("*")
                    OpType.NotEqualTo -> add("!=")
                    OpType.RegexLike -> add("regexp_like()")
                    OpType.Subtract -> add("-")
                }
                add(lhs.asJSON())
                if (type != OpType.Between) {
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

    internal class CompoundExpression(
        private val subexpressions: List<Expression>,
        private val type: OpType
    ) : Expression() {

        internal enum class OpType {
            And, Or, Not
        }

        public override fun asJSON(): Any {
            return buildList {
                when (type) {
                    OpType.And -> add("AND")
                    OpType.Or -> add("OR")
                    OpType.Not -> add("NOT")
                }
                subexpressions.forEach {
                    add(it.asJSON())
                }
            }
        }
    }

    internal class UnaryExpression(
        private val operand: Expression,
        private val type: OpType
    ) : Expression() {

        internal enum class OpType {
            Missing, NotMissing, NotNull, Null, Valued
        }

        public override fun asJSON(): Any {
            val opd = operand.asJSON()
            return buildList {
                when (type) {
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
                    OpType.Valued -> {
                        add("IS VALUED")
                        add(opd)
                    }
                }
            }
        }
    }

    internal class ParameterExpression(val name: String) : Expression() {

        public override fun asJSON(): Any =
            listOf("$$name")
    }

    internal class CollationExpression(
        private val operand: Expression,
        private val collation: Collation
    ) : Expression() {

        public override fun asJSON(): Any {
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

        public override fun asJSON(): Any {
            return buildList {
                add(func)
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
            CompoundExpression(listOf(expression), CompoundExpression.OpType.Not)

        public actual fun not(expression: Expression): Expression =
            negated(expression)
    }

    public actual fun multiply(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OpType.Multiply)

    public actual fun divide(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OpType.Divide)

    public actual fun modulo(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OpType.Modulus)

    public actual fun add(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OpType.Add)

    public actual fun subtract(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OpType.Subtract)

    public actual fun lessThan(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OpType.LessThan)

    public actual fun lessThanOrEqualTo(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OpType.LessThanOrEqualTo)

    public actual fun greaterThan(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OpType.GreaterThan)

    public actual fun greaterThanOrEqualTo(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OpType.GreaterThanOrEqualTo)

    public actual fun equalTo(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OpType.EqualTo)

    public actual fun notEqualTo(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OpType.NotEqualTo)

    public actual fun and(expression: Expression): Expression =
        CompoundExpression(listOf(this, expression), CompoundExpression.OpType.And)

    public actual fun or(expression: Expression): Expression =
        CompoundExpression(listOf(this, expression), CompoundExpression.OpType.Or)

    public actual fun like(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OpType.Like)

    public actual fun regex(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OpType.RegexLike)

    public actual fun `is`(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OpType.Is)

    public actual fun isNot(expression: Expression): Expression =
        BinaryExpression(this, expression, BinaryExpression.OpType.IsNot)

    public actual fun between(expression1: Expression, expression2: Expression): Expression {
        val aggr = AggregateExpression(listOf(expression1, expression2))
        return BinaryExpression(this, aggr, BinaryExpression.OpType.Between)
    }

    public actual fun isValued(): Expression =
        UnaryExpression(this, UnaryExpression.OpType.Valued)

    public actual fun isNotValued(): Expression =
        negated(isValued())

    public actual fun collate(collation: Collation): Expression =
        CollationExpression(this, collation)

    public actual fun `in`(vararg expressions: Expression): Expression {
        if (expressions.isEmpty()) throw IllegalArgumentException("empty 'IN'.")
        val aggr = AggregateExpression(expressions.toList())
        return BinaryExpression(this, aggr, BinaryExpression.OpType.In)
    }

    override fun toString(): String =
        "${this::class.simpleName} {@${hashCode().toString(16)},json=" + asJSON() + "}"

    internal open fun asJSON(): Any? {
        throw IllegalStateException("Should be overridden in subclass ${this::class}")
    }
}
