import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    androidTarget()
    sourceSets {
        androidMain.dependencies {
            implementation(projects.shared)
        }
    }
}

compose {
    kotlinCompilerPlugin.set("1.5.7")
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
}

android {
    namespace = "dev.kotbase.gettingstarted.compose"
    compileSdk = 34
    defaultConfig {
        applicationId = "dev.kotbase.gettingstarted.compose"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    packagingOptions.resources.pickFirsts += "META-INF/**"
}
