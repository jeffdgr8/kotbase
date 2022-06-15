package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass

public actual open class Join
private constructor(actual: com.couchbase.lite.Join) :
    DelegatedClass<com.couchbase.lite.Join>(actual) {

    public actual class On
    internal constructor(override val actual: com.couchbase.lite.Join.On) : Join(actual) {

        public actual fun on(expression: Expression): Join = chain {
            actual.on(expression.actual)
        }
    }

    public actual companion object {

        public actual fun join(datasource: DataSource): On =
            On(com.couchbase.lite.Join.join(datasource.actual))

        public actual fun innerJoin(datasource: DataSource): On =
            On(com.couchbase.lite.Join.innerJoin(datasource.actual))

        public actual fun leftJoin(datasource: DataSource): On =
            On(com.couchbase.lite.Join.leftJoin(datasource.actual))

        public actual fun leftOuterJoin(datasource: DataSource): On =
            On(com.couchbase.lite.Join.leftOuterJoin(datasource.actual))

        public actual fun crossJoin(datasource: DataSource): Join =
            Join(com.couchbase.lite.Join.crossJoin(datasource.actual))
    }
}
