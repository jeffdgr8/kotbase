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

expect object FileUtils {

    fun dirExists(dir: String): Boolean

    fun listFiles(dir: String): List<String>

    fun getCanonicalPath(path: String): String

    fun verifyDir(dirPath: String): String

    fun eraseFileOrDir(fileOrDirectory: String): Boolean

    fun deleteContents(fileOrDirectory: String?): Boolean

    fun write(bytes: ByteArray, path: String)

    fun read(path: String): ByteArray

    val separatorChar: Char
}

fun FileUtils.getParentDir(path: String): String {
    val file = path.dropLastWhile { it == separatorChar }
    return file.take(file.lastIndexOf(separatorChar))
}
