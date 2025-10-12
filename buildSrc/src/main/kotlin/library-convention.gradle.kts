plugins {
    id("base-convention")
    id("dokka-convention")
    id("com.vanniktech.maven.publish")
}

kotlin {
    explicitApiWarning()
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    pom {
        name.set(project.name)
        afterEvaluate { this@pom.description.set(this@afterEvaluate.description) }
        url.set("https://kotbase.dev/")

        licenses {
            license {
                name.set("Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
        }

        developers {
            developer {
                name.set("Jeff Lockhart")
                email.set("jeff@kotbase.dev")
            }
        }

        scm {
            url.set("https://github.com/jeffdgr8/kotbase")
            connection.set("https://github.com/jeffdgr8/kotbase.git")
        }

        issueManagement {
            system.set("GitHub")
            url.set("https://github.com/jeffdgr8/kotbase/issues")
        }
    }
}
