import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("base-convention")
}

kotlin {
    /*
     * Source set hierarchy:
     *                           ┌────────┐
     *                           │ common │
     *                           └───┬────┘
     *                 ┌─────────────┴─────────────┐
     *                 │                    ┌──────┴───────┐
     *                 │                    │ nativeCommon │
     *                 │                    └──────┬───────┘
     *                 │                  ┌────────┴────────┐
     *           ┌─────┴─────┐        ┌───┴───┐         ┌───┴────┐
     *           │ jvmCommon │        │ apple │         │ native │
     *           └─────┬─────┘        └───┬───┘         └───┬────┘
     *            ┌────┴────┐             │           ┌─────┴──────┐
     *       ╔════╩════╗ ╔══╩══╗          │      ╔════╩═════╗ ╔════╩═════╗
     *       ║ android ║ ║ jvm ║          │      ║ linuxX64 ║ ║ mingwX64 ║
     *       ╚═════════╝ ╚═════╝          │      ╚══════════╝ ╚══════════╝
     *      ┌─────────────────┬───────────┴───┬───────────┬─────────────┐
     * ╔════╩═════╗ ╔═════════╩═════════╗ ╔═══╩════╗ ╔════╩═════╗ ╔═════╩══════╗
     * ║ iosArm64 ║ ║ iosSimulatorArm64 ║ ║ iosX64 ║ ║ macosX64 ║ ║ macosArm64 ║
     * ╚══════════╝ ╚═══════════════════╝ ╚════════╝ ╚══════════╝ ╚════════════╝
     */

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    targetHierarchy.custom {
        common {
            group("jvmCommon") {
                withAndroidTarget()
                withJvm()
            }
            group("nativeCommon") {
                group("apple") {
                    withApple()
                }
                group("native") {
                    withLinux()
                    withMingw()
                }
            }
        }
    }

    androidTarget()
    jvm()
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    macosX64()
    macosArm64()
    linuxX64()
    mingwX64()
}
