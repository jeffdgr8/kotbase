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
