package com.couchbase.lite.kmp.internal.utils

import com.soywiz.korio.file.std.applicationVfs
import com.soywiz.korio.file.std.openAsZip
import com.soywiz.korio.stream.*
import kotlinx.coroutines.runBlocking
import okio.Source
import okio.buffer
import okio.use

actual object ZipUtils {

    actual fun unzip(input: Source, destination: String) {
        runBlocking {
            input.asyncStream().openAsZip { zipFile ->
                zipFile.copyRecursively(applicationVfs[destination])
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
