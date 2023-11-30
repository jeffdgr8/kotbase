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

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * A Limit component represents the LIMIT clause of the query statement.
 */
public expect class Limit : Query {

    override var parameters: Parameters?

    override fun execute(): ResultSet

    override fun explain(): String

    override fun addChangeListener(listener: QueryChangeListener): ListenerToken

    override fun addChangeListener(context: CoroutineContext, listener: QueryChangeSuspendListener): ListenerToken

    override fun addChangeListener(scope: CoroutineScope, listener: QueryChangeSuspendListener)

    @Deprecated(
        "Use ListenerToken.remove()",
        ReplaceWith("token.remove()")
    )
    override fun removeChangeListener(token: ListenerToken)
}
