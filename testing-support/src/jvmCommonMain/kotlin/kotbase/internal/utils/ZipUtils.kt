package kotbase.internal.utils

import okio.Source
import okio.buffer
import okio.use
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

actual object ZipUtils {

    actual fun unzip(input: Source, destination: String) {
        val buffer = ByteArray(1024)
        input.use { source ->
            source.buffer().use { bufferedSource ->
                bufferedSource.inputStream().use { inputStream ->
                    ZipInputStream(inputStream).use { zis ->
                        var ze = zis.nextEntry
                        while (ze != null) {
                            val newFile = File(destination, ze.name)
                            if (ze.isDirectory) {
                                newFile.mkdirs()
                            } else {
                                File(newFile.parent!!).mkdirs()
                                FileOutputStream(newFile).use { fos ->
                                    while (true) {
                                        val len = zis.read(buffer)
                                        if (len < 0) break
                                        fos.write(buffer, 0, len)
                                    }
                                }
                            }
                            ze = zis.nextEntry
                        }
                        zis.closeEntry()
                    }
                }
            }
        }
    }
}
