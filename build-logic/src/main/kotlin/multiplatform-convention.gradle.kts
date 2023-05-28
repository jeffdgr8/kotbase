import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("base-convention")
}

kotlin {
    /*
     * Source set hierarchy:
     *                         common
     *                           |
     *          +----------------+----------------+
     *          |                                 |
     *      jvmCommon                        nativeCommon
     *          |                                 |
     *       +--+--+            +-----------------+-----------------+
     *       |     |            |                                   |
     *    android jvm         apple                               native
     *                          |                                   |
     *    +-------------+-------+---+-------+---------+         +---+----+
     *    |             |           |       |         |         |        |
     * iosArm64 iosSimulatorArm64 iosX64 macosX64 macosArm64 linuxX64 mingwX64
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
