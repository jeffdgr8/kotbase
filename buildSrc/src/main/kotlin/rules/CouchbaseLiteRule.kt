package rules

import org.gradle.api.artifacts.CacheableRule
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule
import org.gradle.api.artifacts.dsl.ComponentMetadataHandler
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmEnvironment
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withModule
import javax.inject.Inject

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

fun ComponentMetadataHandler.applyCouchbaseLiteRule(jvmLib: String, androidLib: String) {
    withModule<CouchbaseLiteRule>(jvmLib) {
        params(
            TargetJvmEnvironment.STANDARD_JVM,
            TargetJvmEnvironment.ANDROID,
            androidLib,
        )
    }
    withModule<CouchbaseLiteRule>(androidLib) {
        params(
            TargetJvmEnvironment.ANDROID,
            TargetJvmEnvironment.STANDARD_JVM,
            jvmLib,
        )
    }
}
