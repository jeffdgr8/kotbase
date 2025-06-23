plugins {
    `kotlin-dsl`
}

repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(plugin(libs.plugins.kotlin.multiplatform))
    implementation(plugin(libs.plugins.kotlinx.atomicfu))
    implementation(plugin(libs.plugins.android.library))
    implementation(plugin(libs.plugins.dokka))
    implementation(libs.dokka.versioning)
    implementation(plugin(libs.plugins.vanniktech.maven.publish))
}

fun plugin(provider: Provider<PluginDependency>) = with(provider.get()) {
    "$pluginId:$pluginId.gradle.plugin:$version"
}
