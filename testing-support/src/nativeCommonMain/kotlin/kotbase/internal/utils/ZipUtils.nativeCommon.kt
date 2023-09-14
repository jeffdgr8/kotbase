package kotbase.internal.utils

import korlibs.io.file.std.applicationVfs
import korlibs.io.file.std.openAsZip
import korlibs.io.stream.AsyncStream
import korlibs.io.stream.openAsync
import kotlinx.coroutines.runBlocking
import kotlinx.io.Source
import kotlinx.io.readByteArray

actual object ZipUtils {

    actual fun unzip(input: Source, destination: String) {
        runBlocking {
            input.asyncStream().openAsZip { zipFile ->
                zipFile.copyToRecursively(applicationVfs[destination])
            }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
fun Source.asyncStream(): AsyncStream = use { source ->
    source.use { input ->
        input.readByteArray().openAsync()
    }
}
