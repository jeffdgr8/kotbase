package com.couchbase.lite.kmm.internal.utils

import com.couchbase.lite.LogDomain
import com.couchbase.lite.internal.support.Log
import java.io.File
import java.io.IOException

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

    private fun verifyDir(dir: File): String {
        var dir = dir
        var err: IOException? = null
        try {
            dir = dir.canonicalFile
            if (dir.exists() && dir.isDirectory || dir.mkdirs()) {
                return dir.absolutePath
            }
        } catch (e: IOException) {
            err = e
        }
        throw IllegalStateException("Cannot create or access directory at $dir", err)
    }

    actual fun eraseFileOrDir(fileOrDirectory: String): Boolean {
        return eraseFileOrDir(File(fileOrDirectory))
    }

    private fun eraseFileOrDir(fileOrDirectory: File): Boolean {
        return deleteRecursive(fileOrDirectory)
    }

    actual fun deleteContents(fileOrDirectory: String?): Boolean {
        return deleteContents(if (fileOrDirectory == null) null else File(fileOrDirectory))
    }

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

    private fun deleteRecursive(fileOrDirectory: File): Boolean {
        return !fileOrDirectory.exists() || deleteContents(fileOrDirectory) && fileOrDirectory.delete()
    }
}
