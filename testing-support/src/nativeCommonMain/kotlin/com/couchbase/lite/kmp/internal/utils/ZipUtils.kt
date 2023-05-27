package com.couchbase.lite.kmp.internal.utils

import korlibs.io.file.std.applicationVfs
import korlibs.io.file.std.openAsZip
import korlibs.io.stream.AsyncStream
import korlibs.io.stream.openAsync
import kotlinx.coroutines.runBlocking
import okio.Source
import okio.buffer
import okio.use

actual object ZipUtils {

    actual fun unzip(input: Source, destination: String) {
        runBlocking {
            input.asyncStream().openAsZip { zipFile ->
                zipFile.copyToRecursively(applicationVfs[destination])
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
