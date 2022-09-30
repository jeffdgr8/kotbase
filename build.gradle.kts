plugins {
    id("org.jetbrains.kotlin.multiplatform") version "1.7.20" apply false
    id("org.jetbrains.dokka") version "1.7.10" apply false
    id("com.android.library") version "7.3.0" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
