package com.couchbase.lite.kmp.internal.utils

import com.couchbase.lite.LogDomain
import com.couchbase.lite.internal.support.Log
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

    private fun deleteContents(fileOrDirectory: File?): Boolean {
        if (fileOrDirectory == null || !fileOrDirectory.isDirectory) {
            return true
        }
        val contents = fileOrDirectory.listFiles() ?: return true
        var succeeded = true
        for (file in contents) {
            if (!deleteRecursive(file)) {
                Log.i(LogDomain.DATABASE, "Failed deleting file: $file")
                succeeded = false
            }
        }
        return succeeded
    }

    private fun deleteRecursive(fileOrDirectory: File): Boolean =
        !fileOrDirectory.exists() || deleteContents(fileOrDirectory) && fileOrDirectory.delete()
}
