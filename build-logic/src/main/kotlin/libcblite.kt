import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.internal.catalog.DelegatingProjectDependency
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.testing.Test
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.kpm.external.ExternalVariantApi
import org.jetbrains.kotlin.gradle.kpm.external.project
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.konan.target.Architecture
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget

fun KotlinMultiplatformExtension.useCouchbaseLiteNativeCLib(delegate: DelegatingProjectDependency) =
    useCouchbaseLiteNativeCLib(delegate.dependencyProject)

@OptIn(ExternalVariantApi::class)
fun KotlinMultiplatformExtension.useCouchbaseLiteNativeCLib(fromProject: Project = project) {
    with(project) {
        targets.withType<KotlinNativeTarget> {
            if (konanTarget.family == Family.LINUX) {
                val libraryPath = "${fromProject.projectDir}/$libcbliteLibPath"
                binaries.getTest(DEBUG).linkerOpts += listOf(
                    "-L$libraryPath", "-lcblite", "-rpath", libraryPath
                )
            }
        }

        if (System.getProperty("os.name") == "Linux") {
            tasks.withType<Test> {
                environment(
                    "LD_LIBRARY_PATH",
                    "\$LD_LIBRARY_PATH:$rootDir/libs/libicu-dev/linux/x86_64/libicu-dev-54.1/lib/x86_64-linux-gnu"
                )
            }
        }

        tasks.withType<KotlinNativeTest> {
            val dir = name.substring(0, name.lastIndex - 3)
            if (dir.isWindows) {
                dependsOn(
                    tasks.register<Copy>("copyLibcbliteDll") {
                        val version = libs.versions.couchbase.lite.c.get()
                        from("${fromProject.projectDir}/${libcblitePath(dir.os, dir.arch, version)}/bin/cblite.dll")
                        into("build/bin/$dir/debugTest")
                    }
                )
            }
        }
    }
}

val KotlinNativeTarget.libcbliteLibPath: String
    get() = "$libcblitePath/${konanTarget.libcbliteLib}"

val KotlinNativeTarget.libcbliteIncludePath: String
    get() = "$libcblitePath/include"

private val KotlinNativeTarget.libcblitePath: String
    get() {
        val version = project.libs.versions.couchbase.lite.c.get()
        return libcblitePath(konanTarget.os, konanTarget.arch, version)
    }

private val Project.libs: LibrariesForLibs
    get() = extensions.getByName("libs") as LibrariesForLibs

private val KonanTarget.os: String
    get() {
        return when (family) {
            Family.LINUX -> "linux"
            Family.MINGW -> "windows"
            else -> error("Unhandled native OS: $this")
        }
    }

private val KonanTarget.arch: String
    get() {
        return when (architecture) {
            Architecture.X64 -> "x86_64"
            Architecture.ARM64 -> "arm64"
            Architecture.ARM32 -> "armhf"
            else -> error("Unhandled native architecture: $this")
        }
    }

private val KonanTarget.libcbliteLib: String
    get() {
        return when (family) {
            Family.LINUX -> when (architecture) {
                Architecture.X64 -> "lib/x86_64-linux-gnu"
                Architecture.ARM64 -> "lib/aarch64-linux-gnu"
                Architecture.ARM32 -> "lib/arm-linux-gnueabihf"
                else -> error("Unhandled native architecture: $arch")
            }
            Family.MINGW -> "lib"
            else -> error("Unhandled native OS: $os")
        }
    }

private val String.isWindows: Boolean
    get() = startsWith("mingw")

private val String.os: String
    get() {
        return when {
            startsWith("linux") -> "linux"
            startsWith("mingw") -> "windows"
            else -> error("Unhandled native OS: $this")
        }
    }

private val String.arch: String
    get() {
        return when {
            endsWith("X64") -> "x86_64"
            endsWith("Arm64") -> "arm64"
            endsWith("Arm32") -> "armhf"
            else -> error("Unhandled native architecture: $this")
        }
    }

private fun libcblitePath(os: String, arch: String, version: String): String =
    "libs/libcblite/$os/$arch/libcblite-$version"
