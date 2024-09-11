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
    const val groupId = "org.kikermo.bleserver"
    const val artifactId = "core"

    const val desc = "BLUZ implementation for BleServer"
    const val license = "Apache-2.0"
    const val githubRepo = "kikermo/ble-server"
    const val release = "https://s01.oss.sonatype.org/service/local/"
    const val snapshot = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
}


publishing {
    publications {

        create<MavenPublication>("maven") {
            groupId = PubilsInfo.groupId
            artifactId = PubilsInfo.artifactId
//            version = project.version.toString()
            version = "0.0.1"

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
                        //organization.set("")
                        //organizationUrl.set("https://kikermo.org/")
                    }
                }
                scm {
                    url.set(
                        "https://github.com/${PubilsInfo.githubRepo}.git"
                    )
                    connection.set(
                        "scm:git:git://github.com/${PubilsInfo.githubRepo}.git"
                    )
                    developerConnection.set(
                        "scm:git:git://github.com/${PubilsInfo.githubRepo}.git"
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
