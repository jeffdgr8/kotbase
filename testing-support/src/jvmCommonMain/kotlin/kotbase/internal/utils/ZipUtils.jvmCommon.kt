package kotbase.internal.utils

import kotlinx.io.Source
import kotlinx.io.asInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

actual object ZipUtils {

    actual fun unzip(input: Source, destination: String) {
        val buffer = ByteArray(1024)
        input.use { source ->
            source.asInputStream().use { inputStream ->
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
