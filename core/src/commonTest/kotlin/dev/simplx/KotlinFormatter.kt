package dev.simplx

object KotlinFormatter {
    fun format(format: String, vararg args: Any?): String =
        Formatter().format(format, args).toString()
}
