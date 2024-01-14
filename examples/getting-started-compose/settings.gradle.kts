rootProject.name = "getting-started-compose"

include(":composeApp")

if (settings.extra.properties.getOrDefault("useLocalLib", "false")?.toString().toBoolean()) {
    includeBuild("../..")
}

pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
