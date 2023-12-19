import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("base-convention")
}

kotlin {
    /*
     * Source set hierarchy:
     *                      ┌────────┐
     *                      │ common │
     *                      └───┬────┘
     *               ┌──────────┴───────────┐
     *               │                  ┌───┴────┐
     *               │                  │ native │
     *               │                  └───┬────┘
     *               │           ┌──────────┴──────────┐
     *         ┌─────┴─────┐ ┌───┴───┐           ┌─────┴──────┐
     *         │ jvmCommon │ │ apple │           │ linuxMingw │
     *         └─────┬─────┘ └───┬───┘           └─────┬──────┘
     *          ┌────┴────┐      │       ┌─────────────┼─────────────┐
     *     ╔════╧════╗ ╔══╧══╗   │  ╔════╧═════╗ ╔═════╧══════╗ ╔════╧═════╗
     *     ║ android ║ ║ jvm ║   │  ║ linuxX64 ║ ║ linuxArm64 ║ ║ mingwX64 ║
     *     ╚═════════╝ ╚═════╝   │  ╚══════════╝ ╚════════════╝ ╚══════════╝
     *      ┌─────────────────┬──┴────────────┬───────────┬─────────────┐
     * ╔════╧═════╗ ╔═════════╧═════════╗ ╔═══╧════╗ ╔════╧═════╗ ╔═════╧══════╗
     * ║ iosArm64 ║ ║ iosSimulatorArm64 ║ ║ iosX64 ║ ║ macosX64 ║ ║ macosArm64 ║
     * ╚══════════╝ ╚═══════════════════╝ ╚════════╝ ╚══════════╝ ╚════════════╝
     */

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("jvmCommon") {
                withAndroidTarget()
                withJvm()
            }
            group("native") {
                group("linuxMingw") {
                    group("linux")
                    group("mingw")
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
    linuxArm64()
    mingwX64()
}
