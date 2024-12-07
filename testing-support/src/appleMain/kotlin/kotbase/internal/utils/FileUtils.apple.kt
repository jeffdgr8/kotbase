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
@file:Suppress("INVISIBLE_REFERENCE")

package kotbase.internal.utils

import kotbase.LogDomain
import kotbase.ext.toByteArray
import kotbase.ext.toException
import kotbase.ext.toNSData
import kotbase.ext.wrapError
import kotlinx.cinterop.*
import kotlinx.io.IOException
import platform.Foundation.*

actual object FileUtils {

    private val fm = NSFileManager.defaultManager

    actual fun dirExists(dir: String): Boolean = memScoped {
        val isDir = alloc<BooleanVar>()
        fm.fileExistsAtPath(dir, isDir.ptr) && isDir.value
    }

    actual fun listFiles(dir: String): List<String> {
        try {
            @Suppress("UNCHECKED_CAST")
            val files = wrapError(NSError::toException) { error ->
                fm.contentsOfDirectoryAtPath(dir, error)
            } as List<String>
            val prefix = if (dir.endsWith('/')) dir else "$dir/"
            return files.map { prefix + it }
        } catch (e: Exception) {
            throw IOException(e.message, e)
        }
    }

    actual fun getCanonicalPath(path: String): String =
        NSURL(fileURLWithPath = path).canonicalPath

    private val NSURL.canonicalPath: String
        get() = memScoped {
            val canonicalPath = alloc<ObjCObjectVar<Any?>>()
            val error = alloc<ObjCObjectVar<NSError?>>()
            getResourceValue(canonicalPath.ptr, NSURLCanonicalPathKey, error.ptr)
            val nsError = error.value
            if (nsError != null) {
                if (nsError.code == NSFileReadNoSuchFileError && nsError.domain == NSCocoaErrorDomain) {
                    return path!!
                }
                throw IOException(error.value?.localizedDescription, error.value?.toException())
            }
            canonicalPath.value as String
        }

    actual fun verifyDir(dirPath: String): String =
        verifyDir(NSURL(fileURLWithPath = dirPath))

    private fun verifyDir(dir: NSURL): String {
        val path = dir.canonicalPath
        if (!dirExists(path)) {
            try {
                wrapError(NSError::toException) { error ->
                    fm.createDirectoryAtURL(dir, true, null, error)
                }
            } catch (e: Exception) {
                throw IllegalStateException("Cannot create or access directory at $dir", e)
            }
        }
        return dir.path!!
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
        bytes.toNSData().writeToFile(path, false)
    }

    actual fun read(path: String): ByteArray =
        NSData.dataWithContentsOfFile(path)!!.toByteArray()

    actual val separatorChar: Char
        get() = '/'

    private fun deleteRecursive(fileOrDirectory: String): Boolean =
        !exists(fileOrDirectory) || deleteContents(fileOrDirectory) && delete(fileOrDirectory)

    private fun exists(file: String): Boolean =
        fm.fileExistsAtPath(file)

    private fun delete(file: String): Boolean {
        return try {
            wrapError(NSError::toException) { error ->
                fm.removeItemAtPath(file, error)
            }
        } catch (_: Exception) {
            false
        }
    }
}
