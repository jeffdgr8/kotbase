package com.couchbase.lite.kmm.internal.utils

import com.soywiz.korio.file.std.openAsZip
import com.soywiz.korio.stream.*
import com.udobny.kmm.ext.toException
import com.udobny.kmm.ext.toNSData
import com.udobny.kmm.ext.wrapError
import kotlinx.coroutines.runBlocking
import okio.Source
import okio.buffer
import okio.use
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.writeToURL

actual object ZipUtils {

    actual fun unzip(input: Source, destination: String) {
        val dstUrl = NSURL.fileURLWithPath(destination, true)
        runBlocking {
            input.asyncStream().openAsZip { zipFile ->
                zipFile.listRecursive().collect { file ->
                    val path = file.path.dropWhile { it == '/' }
                    val url = NSURL.fileURLWithPath(path, file.isDirectory(), dstUrl)
                    if (file.isDirectory()) {
                        FileUtils.mkDirs(url)
                    } else {
                        FileUtils.mkDirs(NSURL.fileURLWithPath(file.parent.path, true, dstUrl))
                        val nsData = file.read().toNSData()
                        wrapError(NSError::toException) { error ->
                            nsData.writeToURL(url, 0, error)
                        }
                    }
                }
            }
        }
    }
}

fun Source.asyncStream(): AsyncStream {
    return use { source ->
        source.buffer().use { buffer ->
            buffer.readByteArray().openAsync()
        }
    }
}
