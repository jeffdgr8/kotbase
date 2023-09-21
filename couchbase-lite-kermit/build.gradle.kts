import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `multiplatform-convention`
    `library-convention`
}

kotlin {
    cocoapods {
        name = "Kotbase-Kermit"
        homepage = "https://github.com/jeffdgr8/kotbase"
        authors = "Couchbase, Jeff Lockhart"
        license = "Apache License, Version 2.0"
        summary = "Couchbase Lite for Kotlin Multiplatform Kermit Logger"
        ios.deploymentTarget = "9.0"
        osx.deploymentTarget = "10.11"
        framework {
            baseName = this@cocoapods.name.replace('-', '_')
            isStatic = false
            export(libs.kermit.simple)
        }
        pod("CouchbaseLite") {
            version = libs.versions.couchbase.lite.objc.get()
            linkOnly = true
        }
    }

    linkLibcblite(projects.couchbaseLite)

    sourceSets {
        commonMain {
            dependencies {
                api(projects.couchbaseLiteKtx)
                api(libs.kermit)
            }
        }
        appleMain {
            dependencies {
                api(libs.kermit.simple)
            }
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

android {
    namespace = "dev.kotbase.kermit"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
