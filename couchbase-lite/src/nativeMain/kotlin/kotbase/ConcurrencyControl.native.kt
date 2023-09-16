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

import kotlinx.cinterop.convert
import libcblite.CBLConcurrencyControl
import libcblite.kCBLConcurrencyControlFailOnConflict
import libcblite.kCBLConcurrencyControlLastWriteWins

public actual enum class ConcurrencyControl {
    LAST_WRITE_WINS,
    FAIL_ON_CONFLICT;

    internal val actual: CBLConcurrencyControl
        get() = when (this) {
            LAST_WRITE_WINS -> kCBLConcurrencyControlLastWriteWins
            FAIL_ON_CONFLICT -> kCBLConcurrencyControlFailOnConflict
        }.convert()

    internal companion object {

        internal fun from(value: CBLConcurrencyControl): ConcurrencyControl = when (value.toUInt()) {
            kCBLConcurrencyControlLastWriteWins -> LAST_WRITE_WINS
            kCBLConcurrencyControlFailOnConflict -> FAIL_ON_CONFLICT
            else -> error("Unexpected CBLConcurrencyControl ($value)")
        }
    }
}
