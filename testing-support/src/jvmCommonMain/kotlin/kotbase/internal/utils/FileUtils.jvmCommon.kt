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

import kotbase.LogDomain
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.io.path.Path

actual object FileUtils {

    actual fun dirExists(dir: String): Boolean {
        val file = File(dir)
        return file.exists() && file.isDirectory
    }

    actual fun listFiles(dir: String): List<String> =
        File(dir).listFiles()!!.map { it.absolutePath }

    actual fun getCanonicalPath(path: String): String =
        File(path).canonicalPath

    actual fun verifyDir(dirPath: String): String =
        verifyDir(File(dirPath))

    fun verifyDir(dir: File): String {
        @Suppress("NAME_SHADOWING")
        var dir = dir
        var err: IOException? = null
        try {
            dir = dir.canonicalFile
            if (dir.exists() && dir.isDirectory || dir.mkdirs()) {
                if (!Files.isWritable(Path(dir.path))) {
                    throw IllegalStateException("$dir is not writable")
                }
                return dir.absolutePath
            }
        } catch (e: IOException) {
            err = e
        }
        throw IllegalStateException("Cannot create or access directory at $dir", err)
    }

    actual fun eraseFileOrDir(fileOrDirectory: String): Boolean =
        eraseFileOrDir(File(fileOrDirectory))

    private fun eraseFileOrDir(fileOrDirectory: File): Boolean =
        deleteRecursive(fileOrDirectory)

    actual fun deleteContents(fileOrDirectory: String?): Boolean =
        deleteContents(if (fileOrDirectory == null) null else File(fileOrDirectory))

    actual fun write(bytes: ByteArray, path: String) =
        File(path).writeBytes(bytes)

    actual fun read(path: String): ByteArray =
        File(path).readBytes()

    actual val separatorChar: Char = File.separatorChar

    private fun deleteContents(fileOrDirectory: File?): Boolean {
        if (fileOrDirectory == null || !fileOrDirectory.isDirectory) {
            return true
        }
        val contents = fileOrDirectory.listFiles() ?: return true
        var succeeded = true
        for (file in contents) {
            if (!deleteRecursive(file)) {
                println("${LogDomain.DATABASE} Failed deleting file: $file")
                succeeded = false
            }
        }
        return succeeded
    }

    private fun deleteRecursive(fileOrDirectory: File): Boolean =
        !fileOrDirectory.exists() || deleteContents(fileOrDirectory) && fileOrDirectory.delete()
}
