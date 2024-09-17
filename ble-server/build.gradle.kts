plugins {
    kotlin("jvm")
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
}

dependencies {
}

kotlin {
    jvmToolchain(17)
}

kotlin {
    jvmToolchain(17)
}

java {
    withJavadocJar()
    withSourcesJar()
}

object PubilsInfo {
    const val artifactId = "core"

    const val desc = "BleServer core API"
    const val license = "Apache-2.0"
    const val githubRepo = "kikermo/ble-server"
}

publishing {
    publications {

        create<MavenPublication>("maven") {
            artifactId = PubilsInfo.artifactId

            from(components["java"])

            pom {
                name.set(project.name)
                description.set(PubilsInfo.desc)
                url.set("https://github.com/${PubilsInfo.githubRepo}")
                licenses {
                    license {
                        name.set(PubilsInfo.license)
                        url.set("https://opensource.org/licenses/Apache-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("kikermo")
                        name.set("Enrique Ramirez")
                        // organization.set("")
                        // organizationUrl.set("https://kikermo.org/")
                    }
                }
                scm {
                    url.set(
                        "https://github.com/${PubilsInfo.githubRepo}.git",
                    )
                    connection.set(
                        "scm:git:git://github.com/${PubilsInfo.githubRepo}.git",
                    )
                    developerConnection.set(
                        "scm:git:git://github.com/${PubilsInfo.githubRepo}.git",
                    )
                }
                issueManagement {
                    url.set("https://github.com/${PubilsInfo.githubRepo}/issues")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}
