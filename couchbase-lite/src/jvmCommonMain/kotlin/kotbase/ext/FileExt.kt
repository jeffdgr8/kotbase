package kotbase.ext

import java.io.File

internal fun String.toFile(): File = File(this)
