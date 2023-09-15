package kotbase.internal.utils

import kotbase.LogDomain
import kotlinx.io.IOException
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemPathSeparator
import kotlinx.io.readByteArray
import okio.Path.Companion.toPath
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalStdlibApi::class)
actual object FileUtils {

    actual fun dirExists(dir: String): Boolean {
        val path = Path(dir)
        val fs = SystemFileSystem
        return fs.exists(path) && fs.metadataOrNull(path)?.isDirectory ?: false
    }

    actual fun listFiles(dir: String): List<String> {
        // TODO: use kotlinx-io when list API is available
        try {
            return okio.FileSystem.SYSTEM.list(dir.toPath())
                .map { it.toString() }
        } catch (e: Exception) {
            throw IOException(e.message, e)
        }
    }

    actual fun getCanonicalPath(path: String): String =
        path.toPath().canonicalPath

    // TODO: canonicalize with kotlinx-io when API available
    private val okio.Path.canonicalPath: String
        get() = okio.FileSystem.SYSTEM.canonicalize(this).toString()

    actual fun verifyDir(dirPath: String): String {
        //verifyDir(Path(dirPath))
        val dir = Path(dirPath)

    //private fun verifyDir(dir: Path): String {
        if (!dirExists(dir.toString())) {
            try {
                SystemFileSystem.createDirectories(dir, true)
            } catch (e: Exception) {
                throw IllegalStateException("Cannot create or access directory at $dir", e)
            }
        }
        //return dir.canonicalPath
        return getCanonicalPath(dirPath)
    }

    actual fun eraseFileOrDir(fileOrDirectory: String): Boolean =
        deleteRecursive(fileOrDirectory)

    actual fun deleteContents(fileOrDirectory: String?): Boolean {
        if (fileOrDirectory == null || !dirExists(fileOrDirectory)) {
            return true
        }
        val contents = listFiles(fileOrDirectory)
        var succeeded = true
        for (file in contents) {
            if (!deleteRecursive(file)) {
                println("${LogDomain.DATABASE} Failed deleting file: $file")
                succeeded = false
            }
        }
        return succeeded
    }

    actual fun write(bytes: ByteArray, path: String) {
        SystemFileSystem.sink(Path(path)).buffered().use {
            it.write(bytes)
        }
    }

    actual fun read(path: String): ByteArray {
        return SystemFileSystem.source(Path(path)).buffered().use {
            it.readByteArray()
        }
    }

    @OptIn(ExperimentalNativeApi::class)
    actual val separatorChar: Char
        // workaround https://github.com/Kotlin/kotlinx-io/issues/221
        get() = if (Platform.osFamily == OsFamily.WINDOWS) {
            '\\'
        } else {
            SystemPathSeparator
        }

    private fun deleteRecursive(fileOrDirectory: String): Boolean =
        !exists(fileOrDirectory) || deleteContents(fileOrDirectory) && delete(fileOrDirectory)

    private fun exists(file: String): Boolean =
        SystemFileSystem.exists(Path(file))

    private fun delete(file: String): Boolean {
        return try {
            SystemFileSystem.delete(Path(file), true)
            true
        } catch (e: Exception) {
            false
        }
    }
}
