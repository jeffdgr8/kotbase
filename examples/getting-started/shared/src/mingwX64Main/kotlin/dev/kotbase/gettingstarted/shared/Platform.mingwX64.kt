package dev.kotbase.gettingstarted.shared

class MingwPlatform : Platform {
    override val name: String = "Windows"
}

actual fun getPlatform(): Platform = MingwPlatform()
