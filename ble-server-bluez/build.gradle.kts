plugins {
    id("java")
    kotlin("jvm")
    `maven-publish`
    signing
}

kotlin {
    jvmToolchain(21)
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
    const val ARTIFACT_ID = "bluez"

    const val DESC = "BLUEZ implementation for BleServer"
    const val LICENSE = "Apache-2.0"
    const val GITHUB_REPO = "kikermo/ble-server"
}

publishing {
    publications {

        create<MavenPublication>("maven") {
            artifactId = PubilsInfo.ARTIFACT_ID

            from(components["java"])

            pom {
                name.set(project.name)
                description.set(PubilsInfo.DESC)
                url.set("https://github.com/${PubilsInfo.GITHUB_REPO}")
                licenses {
                    license {
                        name.set(PubilsInfo.LICENSE)
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
                        "https://github.com/${PubilsInfo.GITHUB_REPO}.git",
                    )
                    connection.set(
                        "scm:git:git://github.com/${PubilsInfo.GITHUB_REPO}.git",
                    )
                    developerConnection.set(
                        "scm:git:git://github.com/${PubilsInfo.GITHUB_REPO}.git",
                    )
                }
                issueManagement {
                    url.set("https://github.com/${PubilsInfo.GITHUB_REPO}/issues")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}
