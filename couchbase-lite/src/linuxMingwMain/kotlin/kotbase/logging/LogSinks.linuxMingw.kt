/*
 * Copyright 2025 Jeff Lockhart
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
package kotbase.logging

import libcblite.CBLLogSinks_Console
import libcblite.CBLLogSinks_File
import libcblite.CBLLogSinks_SetConsole
import libcblite.CBLLogSinks_SetCustom
import libcblite.CBLLogSinks_SetFile

public actual object LogSinks {

    public actual var file: FileLogSink?
        get() = CBLLogSinks_File().asFileLogSink()
        set(value) {
            CBLLogSinks_SetFile(value.actual)
        }

    public actual var console: ConsoleLogSink?
        get() = CBLLogSinks_Console().asConsoleLogSink()
        set(value) {
            CBLLogSinks_SetConsole(value.actual)
        }

    public actual var custom: CustomLogSink? = null
        set(value) {
            CBLLogSinks_SetCustom(value.actual)
        }
}
