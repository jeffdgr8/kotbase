package kotbase

import kotlin.Array
import com.couchbase.lite.SelectResult as CBLSelectResult

internal actual class SelectResultPlatformState(
    internal val actual: CBLSelectResult
)

public actual open class SelectResult
private constructor(actual: CBLSelectResult) {

    internal actual val platformState = SelectResultPlatformState(actual)

    public actual class From
    internal constructor(actual: CBLSelectResult.From) : SelectResult(actual) {

        public actual fun from(alias: String): SelectResult {
            actual.from(alias)
            return this
        }
    }

    public actual class As
    internal constructor(actual: CBLSelectResult.As) : SelectResult(actual) {

        public actual fun `as`(alias: String): As {
            actual.`as`(alias)
            return this
        }
    }

    override fun equals(other: Any?): Boolean =
        actual == (other as? SelectResult)?.actual

    override fun hashCode(): Int =
        actual.hashCode()

    override fun toString(): String =
        actual.toString()

    public actual companion object {

        public actual fun property(property: String): As =
            As(CBLSelectResult.property(property))

        public actual fun expression(expression: Expression): As =
            As(CBLSelectResult.expression(expression.actual))

        public actual fun all(): From =
            From(CBLSelectResult.all())
    }
}

internal val SelectResult.actual: CBLSelectResult
    get() = platformState.actual

internal val SelectResult.From.actual: CBLSelectResult.From
    get() = platformState.actual as CBLSelectResult.From

internal val SelectResult.As.actual: CBLSelectResult.As
    get() = platformState.actual as CBLSelectResult.As

internal fun Array<out SelectResult>.actuals(): Array<CBLSelectResult> =
    map { it.actual }.toTypedArray()
