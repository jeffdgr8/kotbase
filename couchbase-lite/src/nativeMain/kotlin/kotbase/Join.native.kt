package kotbase

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
public actual open class Join
private constructor(
    internal val type: Type,
    protected val datasource: DataSource,
    private val on: Expression? = null
) {

    internal fun asJSON(): Map<String, Any?> {
        return buildMap {
            put("JOIN", type.tag)
            if (on != null) {
                put("ON", on.asJSON())
            }
            putAll(datasource.asJSON())
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
