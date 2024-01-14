plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    jvm()
    sourceSets {
        jvmMain.dependencies {
            implementation(projects.shared)
            implementation(compose.desktop.currentOs)
        }
    }
}

compose {
    kotlinCompilerPlugin.set("1.5.7")
    desktop {
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
}
