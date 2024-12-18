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

import cocoapods.CouchbaseLite.CBLQueryDataSource
import cocoapods.CouchbaseLite.CBLQueryExpression
import cocoapods.CouchbaseLite.CBLQueryJoin
import kotbase.internal.DelegatedClass

public actual open class Join
private constructor(actual: CBLQueryJoin) : DelegatedClass<CBLQueryJoin>(actual) {

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
