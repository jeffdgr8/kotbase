package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.Join as CBLJoin

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
public actual open class Join
private constructor(actual: CBLJoin) : DelegatedClass<CBLJoin>(actual) {

    public actual class On
    internal constructor(override val actual: CBLJoin.On) : Join(actual) {

        public actual fun on(expression: Expression): Join {
            actual.on(expression.actual)
            return this
        }
    }

    public actual companion object {

        public actual fun join(datasource: DataSource): On =
            On(CBLJoin.join(datasource.actual))

        public actual fun innerJoin(datasource: DataSource): On =
            On(CBLJoin.innerJoin(datasource.actual))

        public actual fun leftJoin(datasource: DataSource): On =
            On(CBLJoin.leftJoin(datasource.actual))

        public actual fun leftOuterJoin(datasource: DataSource): On =
            On(CBLJoin.leftOuterJoin(datasource.actual))

        public actual fun crossJoin(datasource: DataSource): Join =
            Join(CBLJoin.crossJoin(datasource.actual))
    }
}
