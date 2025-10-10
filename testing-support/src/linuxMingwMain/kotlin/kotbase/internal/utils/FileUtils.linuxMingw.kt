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

import korlibs.io.file.modifiedDate
import korlibs.io.file.std.localVfs
import kotbase.LogDomain
import kotlinx.coroutines.runBlocking
import kotlinx.io.IOException
import kotlinx.io.buffered
import kotlinx.io.files.FileNotFoundException
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemPathSeparator
import kotlinx.io.readByteArray
import kotlin.experimental.ExperimentalNativeApi

actual object FileUtils {

    actual fun dirExists(dir: String): Boolean {
        val path = Path(dir)
        val fs = SystemFileSystem
        return fs.exists(path) && fs.metadataOrNull(path)?.isDirectory == true
    }

    actual fun listFiles(dir: String): List<String> {
        try {
            return SystemFileSystem.list(Path(dir)).map { it.toString() }
        } catch (e: Exception) {
            throw IOException(e.message, e)
        }
    }

    actual fun getCanonicalPath(path: String): String {
        return try {
            SystemFileSystem.resolve(Path(path)).toString()
        } catch (e: FileNotFoundException) {
            // canonical path is in message when file doesn't exist
            e.message!!
        }
    }

    actual fun verifyDir(dirPath: String): String {
        val dir = Path(dirPath)
        if (!dirExists(dir.toString())) {
            try {
                SystemFileSystem.createDirectories(dir, true)
            } catch (e: Exception) {
                throw IllegalStateException("Cannot create or access directory at $dir", e)
            }
        }
        return getCanonicalPath(dirPath)
    }

    actual fun eraseFileOrDir(fileOrDirectory: String): Boolean =
        deleteRecursive(fileOrDirectory)

    actual fun deleteContents(fileOrDirectory: String?): Boolean {
        if (fileOrDirectory == null || !dirExists(fileOrDirectory)) {
            return true
        }
        val contents = listFiles(fileOrDirectory)
        var succeeded = true
        for (file in contents) {
            if (!deleteRecursive(file)) {
                println("${LogDomain.DATABASE} Failed deleting file: $file")
                succeeded = false
            }
        }
        return succeeded
    }

    actual fun write(bytes: ByteArray, path: String) {
        SystemFileSystem.sink(Path(path)).buffered().use {
            it.write(bytes)
        }
    }

    actual fun read(path: String): ByteArray {
        return SystemFileSystem.source(Path(path)).buffered().use {
            it.readByteArray()
        }
    }

    @OptIn(ExperimentalNativeApi::class)
    actual val separatorChar: Char
        // workaround https://github.com/Kotlin/kotlinx-io/issues/221
        get() = if (Platform.osFamily == OsFamily.WINDOWS) {
            '\\'
        } else {
            SystemPathSeparator
        }

    actual fun lastModified(path: String): Long {
        // TODO: use kotlinx-io when last modified API is available
        val vfsFile = localVfs(path)
        return runBlocking {
            vfsFile.stat().modifiedDate.unixMillisLong
        }
    }

    private fun deleteRecursive(fileOrDirectory: String): Boolean =
        !exists(fileOrDirectory) || deleteContents(fileOrDirectory) && delete(fileOrDirectory)

    private fun exists(file: String): Boolean =
        SystemFileSystem.exists(Path(file))

    private fun delete(file: String): Boolean {
        return try {
            SystemFileSystem.delete(Path(file), true)
            true
        } catch (_: Exception) {
            false
        }
    }
}
