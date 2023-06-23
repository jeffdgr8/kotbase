import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.konan.target.Family
import rules.applyCouchbaseLiteRule

plugins {
    `kotlin-multiplatform`
    `android-library`
}

kotlin {
    jvmToolchain(8)

    sourceSets.configureEach {
        languageSettings {
            optIn("kotlin.ExperimentalStdlibApi")
            optIn("kotlin.ExperimentalUnsignedTypes")
            optIn("kotlin.experimental.ExperimentalNativeApi")
            optIn("kotlinx.cinterop.BetaInteropApi")
            optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
    }

    targets.withType<KotlinNativeTarget>().configureEach {
        if (konanTarget.family != Family.MINGW) {
            binaries.configureEach {
                binaryOptions["sourceInfoType"] = "libbacktrace"
            }
        }
    }
}

android {
    compileSdk = 33
    defaultConfig {
        minSdk = 22
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    // required by coroutines 1.7.0 to avoid error:
    // 6 files found with path 'META-INF/LICENSE.md'.
    packagingOptions.resources.pickFirsts += "META-INF/LICENSE*"
    // required until AGP 8.1.0-alpha09+
    // https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains-support
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

tasks.withType<KotlinNativeSimulatorTest>().configureEach {
    device.set("iPhone 14")
}

tasks.withType<KotlinNativeTest> {
    val dir = name.substring(0, name.lastIndex - 3)
    dependsOn(
        tasks.register<Copy>("copy${name.capitalized()}Resources") {
            from("src/commonTest/resources")
            into("build/bin/$dir/debugTest")
        }
    )
}

dependencies {
    components {
        applyCouchbaseLiteRule("com.couchbase.lite:couchbase-lite-java", "com.couchbase.lite:couchbase-lite-android")
        applyCouchbaseLiteRule("com.couchbase.lite:couchbase-lite-java-ee", "com.couchbase.lite:couchbase-lite-android-ee")
    }
}
