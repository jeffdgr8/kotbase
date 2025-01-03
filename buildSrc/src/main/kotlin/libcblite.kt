/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.internal.catalog.DelegatingProjectDependency
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Architecture
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget

fun KotlinMultiplatformExtension.linkLibcblite(delegate: DelegatingProjectDependency) =
    linkLibcblite(project.project(delegate.path))

fun KotlinMultiplatformExtension.linkLibcblite(fromProject: Project = project) {
    with(project) {
        targets.withType<KotlinNativeTarget>().configureEach {
            when (konanTarget.family) {
                Family.LINUX -> {
                    val libraryPath = "${fromProject.projectDir}/$libcbliteLibPath"
                    binaries.getTest(DEBUG).linkerOpts("-L$libraryPath", "-lcblite", "-rpath", libraryPath)
                }
                Family.MINGW -> {
                    binaries.getTest(DEBUG).linkTaskProvider.configure {
                        doLast {
                            val version = libs.versions.couchbase.lite.c.get()
                            val outputDir = outputFile.get().parentFile
                            fromProject.projectDir.resolve("${libcblitePath("windows", "x86_64", version)}/bin/cblite.dll")
                                .copyTo(outputDir.resolve("cblite.dll"), overwrite = true)
                        }
                    }
                }
                else -> {}
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
    get() = when (family) {
        Family.LINUX -> "linux"
        Family.MINGW -> "windows"
        else -> error("Unhandled native OS: $this")
    }

private val KonanTarget.arch: String
    get() = when (architecture) {
        Architecture.X64 -> "x86_64"
        Architecture.ARM64 -> "arm64"
        Architecture.ARM32 -> "armhf"
        else -> error("Unhandled native architecture: $this")
    }

private val KonanTarget.libcbliteLib: String
    get() = when (family) {
        Family.LINUX -> when (architecture) {
            Architecture.X64 -> "lib/x86_64-linux-gnu"
            Architecture.ARM64 -> "lib/aarch64-linux-gnu"
            Architecture.ARM32 -> "lib/arm-linux-gnueabihf"
            else -> error("Unhandled native architecture: $arch")
        }
        Family.MINGW -> "lib"
        else -> error("Unhandled native OS: $os")
    }

private fun libcblitePath(os: String, arch: String, version: String): String =
    "vendor/libcblite/$os/$arch/libcblite-$version"
