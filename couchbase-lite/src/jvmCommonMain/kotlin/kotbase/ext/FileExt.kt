package kotbase.ext

import java.io.File

public fun String.toFile(): File = File(this)
