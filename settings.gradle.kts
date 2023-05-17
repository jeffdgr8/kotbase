pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
    }
}

rootProject.name = "couchbase-lite-kmp"
include(":couchbase-lite", ":couchbase-lite-ee")
include(":couchbase-lite-ktx", ":couchbase-lite-ee-ktx")
include(":couchbase-lite-paging", ":couchbase-lite-ee-paging")
include(":testing-support", ":testing-support-ee")

// Add gradle module metadata to differentiate between Couchbase Lite Java and Android variants
// Fixes false positive error in IDE in CouchbaseLite.init() in androidMain
@CacheableRule
abstract class CouchbaseLiteRule @Inject constructor(
    private val thisTarget: String,
    private val otherTarget: String,
    private val otherDependency: String,
) : ComponentMetadataRule {
    @get:Inject
    protected abstract val objects: ObjectFactory

    override fun execute(context: ComponentMetadataContext) {
        for ((name, usage) in arrayOf("compile" to Usage.JAVA_API, "runtime" to Usage.JAVA_RUNTIME)) {
            context.details.withVariant(name) {
                attributes {
                    attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(thisTarget))
                }
            }
            context.details.addVariant("$name-$otherTarget") {
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, objects.named(usage))
                    attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(otherTarget))
                }
                withDependencies {
                    add(otherDependency) {
                        version { require(context.details.id.version) }
                    }
                }
            }
        }
    }
}

dependencyResolutionManagement {
    components {
        val cblJava = "com.couchbase.lite:couchbase-lite-java"
        val cblAndroid = "com.couchbase.lite:couchbase-lite-android"
        withModule<CouchbaseLiteRule>(cblJava) {
            params(
                TargetJvmEnvironment.STANDARD_JVM,
                TargetJvmEnvironment.ANDROID,
                cblAndroid,
            )
        }
        withModule<CouchbaseLiteRule>(cblAndroid) {
            params(
                TargetJvmEnvironment.ANDROID,
                TargetJvmEnvironment.STANDARD_JVM,
                cblJava,
            )
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
