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
package kotbase.internal.utils

import korlibs.io.file.std.applicationVfs
import korlibs.io.file.std.openAsZip
import korlibs.io.stream.AsyncStream
import korlibs.io.stream.openAsync
import kotlinx.coroutines.runBlocking
import kotlinx.io.Source
import kotlinx.io.readByteArray

actual object ZipUtils {

    actual fun unzip(input: Source, destination: String) {
        runBlocking {
            input.asyncStream().openAsZip { zipFile ->
                zipFile.copyToRecursively(applicationVfs[destination])
            }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
fun Source.asyncStream(): AsyncStream = use { source ->
    source.use { input ->
        input.readByteArray().openAsync()
    }
}
