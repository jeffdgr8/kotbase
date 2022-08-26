@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite.kmp.internal.utils

expect object FileUtils {

    fun dirExists(dir: String): Boolean

    fun listFiles(dir: String): List<String>

    fun getCanonicalPath(path: String): String

    fun verifyDir(dirPath: String): String

    fun eraseFileOrDir(fileOrDirectory: String): Boolean

    fun deleteContents(fileOrDirectory: String?): Boolean

    fun write(bytes: ByteArray, path: String)

    fun read(path: String): ByteArray
}

@Suppress("unused")
fun FileUtils.getParentDir(path: String): String {
    val file = path.dropLastWhile { it == '/' }
    return file.substring(0, file.lastIndexOf('/'))
}
