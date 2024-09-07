import org.jreleaser.model.Active

plugins {
    id("java")
    kotlin("jvm")
    `maven-publish`
//    signing
    id("org.jreleaser") version "1.14.0"
}

kotlin {
    jvmToolchain(17)
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
    const val groupId = "org.kikermo.bleserver"
    const val artifactId = "bluez"

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
//            artifact(tasks["sourcesJar"])
//            artifact(tasks["javadocJar"])
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


jreleaser {
    dryrun = false

    // Used for creating a tagged release, uploading files and generating changelog.
    // In the future we can set this up to push release tags to GitHub, but for now it's
    // set up to do nothing.
    // https://jreleaser.org/guide/latest/reference/release/index.html
    release {
        generic {
            enabled = true
            skipRelease = true
        }
    }

    // Used to announce a release to configured announcers.
    // https://jreleaser.org/guide/latest/reference/announce/index.html
    announce {
        active = Active.NEVER
    }

    // Signing configuration.
    // https://jreleaser.org/guide/latest/reference/signing.html
    signing {
        active = Active.ALWAYS
        armored = true
        publicKey.set(File("public.pgp").readText())
        secretKey.set(File("private.pgp").readText())
    }

    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    setActive( "ALWAYS")
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository("target/staging-deploy")
                }
            }
        }
    }
}


//publishing {
//    publications {
//        create<MavenPublication>("maven") {
//            groupId = "org.kikermo.bleserver"
//            artifactId = "core"
//            version = libs.versions.bleserver.get()
//
//            from(components["kotlin"])
//        }
//    }
//}
