plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jetbrains.compose)
}

dependencies {
    implementation(projects.shared)
    implementation(compose.desktop.currentOs)
}

kotlin {
    jvmToolchain(8)
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            packageName ="Kotbase"
        }
        buildTypes.release.proguard {
            configurationFiles.from("proguard-rules.pro")
        }
    }
}
