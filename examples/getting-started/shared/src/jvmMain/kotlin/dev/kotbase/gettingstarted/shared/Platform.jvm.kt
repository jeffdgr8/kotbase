package dev.kotbase.gettingstarted.shared

class JvmPlatform : Platform {
    override val name: String = "JVM ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JvmPlatform()
