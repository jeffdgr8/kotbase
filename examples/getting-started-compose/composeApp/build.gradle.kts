import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    kotlin("native.cocoapods")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget()
    jvm()
    iosArm64()
    iosX64()
    iosSimulatorArm64()

    cocoapods {
        version = "1.0"
        summary = "Kotbase Getting Started Compose Multiplatform"
        homepage = "https://kotbase.dev/"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
        framework {
            binaryOption("bundleId", "dev.kotbase.gettingstarted.compose")
        }
        pod("CouchbaseLite") {
            version = libs.versions.couchbase.lite.objc.get()
            linkOnly = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotbase)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
        }
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            api(libs.androidx.activity.compose)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
}

android {
    namespace = "dev.kotbase.gettingstarted.compose"
    compileSdk = 35
    defaultConfig {
        applicationId = "dev.kotbase.gettingstarted.compose"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
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
