package kotbase

import cocoapods.CouchbaseLite.CBLQueryDataSource
import cocoapods.CouchbaseLite.CBLQueryExpression
import cocoapods.CouchbaseLite.CBLQueryJoin
import kotbase.base.DelegatedClass

public actual open class Join
private constructor(actual: CBLQueryJoin) :
    DelegatedClass<CBLQueryJoin>(actual) {

    public actual class On
    internal constructor(
        private val join: (CBLQueryDataSource, CBLQueryExpression?) -> CBLQueryJoin,
        private val datasource: CBLQueryDataSource
    ) : Join(join(datasource, null)) {

        public actual fun on(expression: Expression): Join =
            Join(join(datasource, expression.actual))
    }

    public actual companion object {

        public actual fun join(datasource: DataSource): On =
            On(CBLQueryJoin.Companion::join, datasource.actual)

        public actual fun innerJoin(datasource: DataSource): On =
            On(CBLQueryJoin.Companion::innerJoin, datasource.actual)

        public actual fun leftJoin(datasource: DataSource): On =
            On(CBLQueryJoin.Companion::leftJoin, datasource.actual)

        public actual fun leftOuterJoin(datasource: DataSource): On =
            On(CBLQueryJoin.Companion::leftOuterJoin, datasource.actual)

        public actual fun crossJoin(datasource: DataSource): Join =
            Join(CBLQueryJoin.crossJoin(datasource.actual))
    }
}
