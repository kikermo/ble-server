plugins {
    id("java")
    kotlin("jvm")
    `maven-publish`
    signing
}

kotlin {
    jvmToolchain(17)
}

java {
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":ble-server"))

    implementation(platform(libs.dbus.bom))
    implementation(libs.dbus.core)
    implementation(libs.dbus.transport.jnrunixsockets)
//    implementation(libs.dbus.transport.junixsocket)
//    implementation(libs.dbus.transport.nativeunixsockets)

    implementation(libs.log4j.api)
    implementation(libs.log4j.impl)

    testImplementation(libs.junit.core)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

object PubilsInfo {
    const val artifactId = "bluez"

    const val desc = "BLUEZ implementation for BleServer"
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
