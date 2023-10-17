package dev.kotbase.gettingstarted.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
