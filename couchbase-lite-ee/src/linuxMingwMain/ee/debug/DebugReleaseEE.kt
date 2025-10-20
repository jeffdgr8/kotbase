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
package debug

import cnames.structs.CBLEncryptable
import cnames.structs.CBLIndexUpdater
import cnames.structs.CBLTLSIdentity
import cnames.structs.CBLURLEndpointListener
import cnames.structs.CBLVectorEncoding
import kotlinx.cinterop.CPointer
import libcblite.CBLEncryptable_Release
import libcblite.CBLIndexUpdater_Release
import libcblite.CBLTLSIdentity_Release
import libcblite.CBLURLEndpointListener_Release
import libcblite.CBLVectorEncoding_Free

internal fun CBLEncryptable_Release(t: CPointer<CBLEncryptable>?) {
    RefTracker.trackRelease(t, "CBLEncryptable")
    CBLEncryptable_Release(t)
}

internal fun CBLIndexUpdater_Release(t: CPointer<CBLIndexUpdater>?) {
    RefTracker.trackRelease(t, "CBLIndexUpdater")
    CBLIndexUpdater_Release(t)
}

internal fun CBLTLSIdentity_Release(t: CPointer<CBLTLSIdentity>?) {
    RefTracker.trackRelease(t, "CBLTLSIdentity")
    CBLTLSIdentity_Release(t)
}

internal fun CBLURLEndpointListener_Release(t: CPointer<CBLURLEndpointListener>?) {
    RefTracker.trackRelease(t, "CBLURLEndpointListener")
    CBLURLEndpointListener_Release(t)
}

internal fun CBLVectorEncoding_Free(arg0: CPointer<CBLVectorEncoding>?) {
    RefTracker.trackRelease(arg0, "CBLVectorEncoding")
    CBLVectorEncoding_Free(arg0)
}
