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

import kotbase.internal.utils.FileUtils
import kotlinx.cinterop.convert
import platform.Foundation.NSFileManager
import platform.Foundation.temporaryDirectory
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_time

actual abstract class PlatformTest {

    companion object {
        const val SCRATCH_DIR_NAME = "/cbl_test_scratch"
    }

    actual fun setupPlatform() {
        val console = Database.log.console
        console.level = LogLevel.WARNING
        console.domains = LogDomain.ALL_DOMAINS
    }

    actual val tmpDir: String
        get() = FileUtils.verifyDir(
            NSFileManager.defaultManager.temporaryDirectory.path!! + SCRATCH_DIR_NAME
        )

    actual fun executeAsync(delayMs: Long, task: () -> Unit) {
        dispatch_after(
            dispatch_time(DISPATCH_TIME_NOW, delayMs),
            dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.convert(), 0.convert()),
            task
        )
    }
}