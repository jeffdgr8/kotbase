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

import kotlinx.io.Source
import kotlinx.io.asInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

actual object ZipUtils {

    actual fun unzip(input: Source, destination: String) {
        val buffer = ByteArray(1024)
        input.use { source ->
            source.asInputStream().use { inputStream ->
                ZipInputStream(inputStream).use { zis ->
                    var ze = zis.nextEntry
                    while (ze != null) {
                        val newFile = File(destination, ze.name)
                        if (ze.isDirectory) {
                            newFile.mkdirs()
                        } else {
                            File(newFile.parent!!).mkdirs()
                            FileOutputStream(newFile).use { fos ->
                                while (true) {
                                    val len = zis.read(buffer)
                                    if (len < 0) break
                                    fos.write(buffer, 0, len)
                                }
                            }
                        }
                        ze = zis.nextEntry
                    }
                    zis.closeEntry()
                }
            }
        }
    }
}
