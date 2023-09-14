package kotbase.internal.utils

import kotlinx.io.Source

expect object ZipUtils {

    fun unzip(input: Source, destination: String)
}
