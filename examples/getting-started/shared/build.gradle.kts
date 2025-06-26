import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kmp.native.coroutines)
}

kotlin {
    androidTarget()
    jvm()
    iosArm64 {
        binaries.framework {
            baseName = "shared"
            val path = "$rootDir/vendor/CouchbaseLite/CouchbaseLite.xcframework/ios-arm64"
            linkerOpts("-F$path", "-framework", "CouchbaseLite", "-rpath", path)
            binaryOption("bundleId", "dev.kotbase.gettingstarted")
        }
    }
    listOf(
        iosX64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            val path = "$rootDir/vendor/CouchbaseLite/CouchbaseLite.xcframework/ios-arm64_x86_64-simulator"
            linkerOpts("-F$path", "-framework", "CouchbaseLite", "-rpath", path)
            binaryOption("bundleId", "dev.kotbase.gettingstarted")
        }
    }
    macosX64()
    macosArm64()
    linuxX64()
    linuxArm64()
    mingwX64()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotbase)
        }
        configureEach {
            languageSettings {
                if (!name.startsWith("common") &&
                    !name.startsWith("jvm") &&
                    !name.startsWith("android")
                ) {
                    optIn("kotlin.experimental.ExperimentalObjCName")
                    optIn("kotlinx.cinterop.ExperimentalForeignApi")
                }
            }
        }
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
}

android {
    namespace = "dev.kotbase.gettingstarted.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
