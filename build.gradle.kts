plugins {
    kotlin("multiplatform") version "1.9.0-Beta-152" apply false
    id("org.jetbrains.dokka") version "1.8.10" apply false
    id("com.android.library") version "7.4.2" apply false
    id("com.louiscad.complete-kotlin") version "1.1.0"
}

allprojects {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
    }
    group = property("GROUP") as String
    val cblVersion = rootProject.libs.versions.couchbase.lite.java.get()
    val kmpVersion = property("VERSION") as String
    version = "$cblVersion-$kmpVersion"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
