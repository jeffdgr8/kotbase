package com.couchbase.lite.kmm.internal.utils

import com.couchbase.lite.kmm.LogDomain
import com.udobny.kmm.ext.toException
import com.udobny.kmm.ext.wrapError
import kotlinx.cinterop.*
import okio.IOException
import platform.Foundation.*

actual object FileUtils {

    actual fun dirExists(dir: String): Boolean {
        return memScoped {
            val isDir = alloc<BooleanVar>()
            NSFileManager.defaultManager.fileExistsAtPath(dir, isDir.ptr) && isDir.value
        }
    }

    actual fun listFiles(dir: String): List<String> {
        try {
            @Suppress("UNCHECKED_CAST")
            val files = wrapError(NSError::toException) { error ->
                NSFileManager.defaultManager.contentsOfDirectoryAtPath(dir, error)
            } as List<String>
            val prefix = if (dir.endsWith('/')) dir else "$dir/"
            return files.map { prefix + it }
        } catch (e: Exception) {
            throw IOException(e.message, e)
        }
    }

    actual fun getCanonicalPath(path: String): String {
        try {
            return NSURL(fileURLWithPath = path).canonicalPath
        } catch (e: Exception) {
            println("Exception getCanonicalPath($path)")
            println(e)
            e.printStackTrace()
            throw e
        }
    }

    private val NSURL.canonicalPath: String
        get() = memScoped {
            val canonicalPath = alloc<ObjCObjectVar<Any?>>()
            val error = alloc<ObjCObjectVar<NSError?>>()
            getResourceValue(canonicalPath.ptr, NSURLCanonicalPathKey, error.ptr)
            val nsError = error.value
            if (nsError != null) {
                println("error = $nsError")
                if (nsError.code == NSFileReadNoSuchFileError && nsError.domain == NSCocoaErrorDomain) {
                    println("original path = $path")
                    return path!!
                }
                throw IOException(error.value?.localizedDescription, error.value?.toException())
            }
            println("canonicalPath = ${canonicalPath.value}")
            canonicalPath.value as String
        }

    actual fun mkDirs(path: String): Boolean {
        return try {
            return wrapError(NSError::toException) { error ->
                NSFileManager.defaultManager.createDirectoryAtPath(path, true, null, error)
            }
        } catch (e: Exception) {
            false
        }
    }

    fun mkDirs(url: NSURL): Boolean {
        return try {
            return wrapError(NSError::toException) { error ->
                NSFileManager.defaultManager.createDirectoryAtURL(url, true, null, error)
            }
        } catch (e: Exception) {
            false
        }
    }

    actual fun verifyDir(dirPath: String): String =
        verifyDir(NSURL(fileURLWithPath = dirPath))

    private fun verifyDir(dir: NSURL): String {
        val path = dir.canonicalPath
        if (!dirExists(path)) {
            try {
                wrapError(NSError::toException) { error ->
                    NSFileManager.defaultManager.createDirectoryAtURL(dir, true, null, error)
                }
            } catch (e: Exception) {
                throw IllegalStateException("Cannot create or access directory at $dir", e)
            }
        }
        return dir.path!!
    }

    actual fun eraseFileOrDir(fileOrDirectory: String): Boolean {
        return deleteRecursive(fileOrDirectory)
    }

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

    private fun deleteRecursive(fileOrDirectory: String): Boolean {
        return !exists(fileOrDirectory) || deleteContents(fileOrDirectory) && delete(fileOrDirectory)
    }

    private fun exists(file: String): Boolean =
        NSFileManager.defaultManager.fileExistsAtPath(file)

    private fun delete(file: String): Boolean {
        return try {
            wrapError(NSError::toException) { error ->
                NSFileManager.defaultManager.removeItemAtPath(file, error)
            }
        } catch (e: Exception) {
            false
        }
    }
}
