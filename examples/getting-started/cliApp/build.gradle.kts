plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    val cblVersion = libs.versions.couchbase.lite.c.get()
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val linkerOpts: List<String>
    // Get the host system's native target and its CBL linkerOpts
    val nativeTarget = when {
        hostOs == "Mac OS X" -> {
            val path = "$rootDir/vendor/CouchbaseLite/CouchbaseLite.xcframework/macos-arm64_x86_64"
            linkerOpts = listOf("-F$path", "-framework", "CouchbaseLite", "-rpath", path)
            when (val arch = System.getProperty("os.arch")) {
                "x86_64" -> macosX64("native")
                "aarch64" -> macosArm64("native")
                else -> throw GradleException("Unknown macOS architecture: $arch")
            }
        }
        hostOs == "Linux" -> {
            val path = "$projectDir/vendor/libcblite/linux/x86_64/libcblite-$cblVersion/lib/x86_64-linux-gnu"
            linkerOpts = listOf("-L$path", "-lcblite", "-rpath", path)
            when (val arch = System.getProperty("os.arch")) {
                "x86_64" -> linuxX64("native")
                "aarch64" -> linuxArm64("native")
                else -> throw GradleException("Unknown Linux architecture: $arch")
            }
        }
        isMingwX64 -> {
            val path = "$projectDir/vendor/libcblite/windows/x86_64/libcblite-$cblVersion/lib"
            linkerOpts = listOf("-L$path", "-lcblite")
            mingwX64("native")
        }
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.binaries.executable {
        entryPoint = "main"
        linkerOpts(linkerOpts)
        if (isMingwX64) {
            // Windows doesn't have a rpath linker option, copy the .dll to the .exe build path
            linkTaskProvider.configure {
                doLast {
                    projectDir.resolve("vendor/libcblite/windows/x86_64/libcblite-$cblVersion/bin/cblite.dll")
                        .copyTo(outputDirectory.resolve("cblite.dll"), overwrite = true)
                }
            }
        }
        runTask?.run {
            // Get command-line arguments from Gradle properties
            val inputValue = providers.gradleProperty("inputValue").getOrElse("")
            val replicate = providers.gradleProperty("replicate").getOrElse("false")
            argumentProviders.add {
                listOf(inputValue, replicate)
            }
        }
    }

    sourceSets {
        named("nativeMain") {
            dependencies {
                implementation(projects.shared)
            }
        }
    }
}
