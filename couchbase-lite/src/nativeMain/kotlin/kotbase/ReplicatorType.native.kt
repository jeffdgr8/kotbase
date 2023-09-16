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
import libcblite.CBLReplicatorType
import libcblite.kCBLReplicatorTypePull
import libcblite.kCBLReplicatorTypePush
import libcblite.kCBLReplicatorTypePushAndPull

public actual enum class ReplicatorType {
    PUSH_AND_PULL,
    PUSH,
    PULL;

    internal val actual: CBLReplicatorType
        get() = when (this) {
            PUSH_AND_PULL -> kCBLReplicatorTypePushAndPull
            PUSH -> kCBLReplicatorTypePush
            PULL -> kCBLReplicatorTypePull
        }.convert()

    internal companion object {

        internal fun from(replicatorType: CBLReplicatorType): ReplicatorType = when (replicatorType.toUInt()) {
            kCBLReplicatorTypePushAndPull -> PUSH_AND_PULL
            kCBLReplicatorTypePush -> PUSH
            kCBLReplicatorTypePull -> PULL
            else -> error("Unexpected CBLReplicatorType ($replicatorType)")
        }
    }
}
