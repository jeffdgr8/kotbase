package com.couchbase.lite.kmm.internal.utils

expect object FileUtils {

    fun dirExists(dir: String): Boolean

    fun listFiles(dir: String): List<String>

    fun getCanonicalPath(path: String): String

    fun mkDirs(path: String): Boolean

    fun verifyDir(dirPath: String): String

    fun eraseFileOrDir(fileOrDirectory: String): Boolean

    fun deleteContents(fileOrDirectory: String?): Boolean
}

fun FileUtils.getParentDir(path: String): String {
    val file = path.dropLastWhile { it == '/' }
    return file.substring(0, file.lastIndexOf('/'))
}
