package dev.kotbase.gettingstarted.shared

import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object Log {

    private val _output = MutableStateFlow("")
    val output = _output.asStateFlow()

    fun i(tag: String, msg: String) {
        val log = "${timestamp()} I/$tag: $msg"
        println(log)
        _output.value = if (output.value.isNotEmpty()) "${output.value}\n$log" else log
    }

    private fun timestamp(): String =
        Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toString()
            .substring(5, 23)
            .replace('T', ' ')
}
