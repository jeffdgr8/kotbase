import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kmp.native.coroutines)
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    targetHierarchy.default()

    androidTarget()
    jvm()
    iosArm64 {
        binaries.framework {
            baseName = "shared"
            val path = "$rootDir/vendor/CouchbaseLite/CouchbaseLite.xcframework/ios-arm64"
            linkerOpts("-F$path", "-framework", "CouchbaseLite", "-rpath", path)
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
        }
    }
    macosX64()
    macosArm64()
    linuxX64()
    linuxArm64()
    mingwX64()

    sourceSets {
        commonMain {
            dependencies {
                val useLocalLib: String by project
                if (useLocalLib.toBoolean()) {
                    api(libs.kotbase.get().module.toString())
                } else {
                    api(libs.kotbase)
                }
            }
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

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

android {
    namespace = "dev.kotbase.gettingstarted.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
