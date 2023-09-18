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

import cocoapods.CouchbaseLite.CBLLog
import kotbase.internal.DelegatedClass

public actual class Log
internal constructor(actual: CBLLog) : DelegatedClass<CBLLog>(actual) {

    public actual val console: ConsoleLogger by lazy {
        ConsoleLogger(actual.console)
    }

    public actual val file: FileLogger by lazy {
        FileLogger(actual.file)
    }

    public actual var custom: Logger? = null
        set(value) {
            field = value
            actual.custom = value?.convert()
        }
}
