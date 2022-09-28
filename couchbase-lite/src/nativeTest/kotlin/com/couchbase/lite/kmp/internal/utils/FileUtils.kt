package com.couchbase.lite.kmp.internal.utils

import com.couchbase.lite.kmp.LogDomain
import com.soywiz.korio.file.File_separatorChar
import com.soywiz.korio.file.std.cwdVfs
import kotlinx.coroutines.runBlocking
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath

actual object FileUtils {

    actual fun dirExists(dir: String): Boolean {
        return runBlocking {
            cwdVfs[dir].run {
                exists() && isDirectory()
            }
        }
    }

    actual fun listFiles(dir: String): List<String> {
        try {
            val files = runBlocking {
                cwdVfs[dir].listNames()
            }
            val prefix = if (dir.endsWith('/')) dir else "$dir/"
            return files.map { prefix + it }
        } catch (e: Exception) {
            throw IOException(e.message, e)
        }
    }

    actual fun getCanonicalPath(path: String): String {
        try {
            return path.toPath().canonicalPath
        } catch (e: Exception) {
            println("Exception getCanonicalPath($path)")
            println(e)
            e.printStackTrace()
            throw e
        }
    }

    private val Path.canonicalPath: String
        get() = FileSystem.SYSTEM.canonicalize(this).name

    actual fun verifyDir(dirPath: String): String =
        verifyDir(dirPath.toPath())

    private fun verifyDir(dir: Path): String {
        val path = dir.canonicalPath
        if (!dirExists(path)) {
            try {
                FileSystem.SYSTEM.createDirectories(dir, true)
            } catch (e: Exception) {
                throw IllegalStateException("Cannot create or access directory at $dir", e)
            }
        }
        return path
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
        runBlocking {
            cwdVfs[path].write(bytes)
        }
    }

    actual fun read(path: String): ByteArray {
        return runBlocking {
            cwdVfs[path].read()
        }
    }

    actual val separatorChar: Char
        get() = File_separatorChar

    private fun deleteRecursive(fileOrDirectory: String): Boolean {
        return !exists(fileOrDirectory) || deleteContents(fileOrDirectory) && delete(fileOrDirectory)
    }

    private fun exists(file: String): Boolean {
        return runBlocking {
            cwdVfs[file].exists()
        }
    }

    private fun delete(file: String): Boolean {
        return runBlocking {
            cwdVfs[file].delete()
        }
    }
}
