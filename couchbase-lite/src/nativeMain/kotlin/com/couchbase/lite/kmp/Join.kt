package com.couchbase.lite.kmp

public actual open class Join
private constructor(
    internal val type: Type,
    internal val datasource: DataSource,
    internal val on: Expression? = null
) {

    internal fun asJSON(): Dictionary {
        return MutableDictionary().apply {
            setString("JOIN", type.tag)
            val datasource = datasource.asJSON()
            datasource.forEach { key ->
                setValue(key, datasource.getValue(key))
            }
        }
    }

    internal enum class Type(val tag: String) {
        INNER("INNER"),
        LEFT_OUTER("LEFT OUTER"),
        CROSS("CROSS")
    }

    public actual class On
    internal constructor(
        type: Type,
        datasource: DataSource
    ) : Join(type, datasource) {

        public actual fun on(expression: Expression): Join =
            Join(type, datasource, expression)
    }

    public actual companion object {

        public actual fun join(datasource: DataSource): On =
            innerJoin(datasource)

        public actual fun innerJoin(datasource: DataSource): On =
            On(Type.INNER, datasource)

        public actual fun leftJoin(datasource: DataSource): On =
            leftOuterJoin(datasource)

        public actual fun leftOuterJoin(datasource: DataSource): On =
            On(Type.LEFT_OUTER, datasource)

        public actual fun crossJoin(datasource: DataSource): Join =
            Join(Type.CROSS, datasource)
    }
}
