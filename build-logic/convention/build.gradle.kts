plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(plugin(libs.plugins.kotlin.multiplatform))
    implementation(plugin(libs.plugins.kotlinx.atomicfu))
    implementation(plugin(libs.plugins.android.kotlin.multiplatform.library))
    implementation(plugin(libs.plugins.dokka))
    implementation(libs.dokka.versioning)
    implementation(plugin(libs.plugins.vanniktech.maven.publish))
}

fun plugin(provider: Provider<PluginDependency>) = with(provider.get()) {
    "$pluginId:$pluginId.gradle.plugin:$version"
}
