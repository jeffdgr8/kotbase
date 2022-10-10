package com.couchbase.lite.kmp.internal.utils

import com.couchbase.lite.kmp.LogDomain
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.DIRECTORY_SEPARATOR
import okio.Path.Companion.toPath

actual object FileUtils {

    actual fun dirExists(dir: String): Boolean {
        val path = dir.toPath()
        val fs = FileSystem.SYSTEM
        return fs.exists(path) && fs.metadata(path).isDirectory
    }

    actual fun listFiles(dir: String): List<String> {
        try {
            val files = FileSystem.SYSTEM.list(dir.toPath())
            val prefix = if (dir.endsWith('/')) dir else "$dir/"
            return files.map { prefix + it.name }
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
        get() = FileSystem.SYSTEM.canonicalize(this).toString()

    actual fun verifyDir(dirPath: String): String =
        verifyDir(dirPath.toPath())

    private fun verifyDir(dir: Path): String {
        if (!dirExists(dir.name)) {
            try {
                FileSystem.SYSTEM.createDirectories(dir, true)
            } catch (e: Exception) {
                throw IllegalStateException("Cannot create or access directory at $dir", e)
            }
        }
        return dir.canonicalPath
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
        FileSystem.SYSTEM.write(path.toPath(), false) {
            write(bytes)
        }
    }

    actual fun read(path: String): ByteArray {
        return FileSystem.SYSTEM.read(path.toPath()) {
            readByteArray()
        }
    }

    actual val separatorChar: Char
        get() = DIRECTORY_SEPARATOR.first()

    private fun deleteRecursive(fileOrDirectory: String): Boolean =
        !exists(fileOrDirectory) || deleteContents(fileOrDirectory) && delete(fileOrDirectory)

    private fun exists(file: String): Boolean =
        FileSystem.SYSTEM.exists(file.toPath())

    private fun delete(file: String): Boolean {
        return try {
            FileSystem.SYSTEM.delete(file.toPath(), true)
            true
        } catch (e: Exception) {
            false
        }
    }
}
