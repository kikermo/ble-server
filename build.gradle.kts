plugins {
    kotlin("jvm") version "2.0.0"
    // id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("org.jreleaser") version "1.14.0"
}

group = "org.kikermo.bleserver"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(11)
}

object Meta {
    const val release = "https://s01.oss.sonatype.org/service/local/"
    const val snapshot = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
}

jreleaser {
    signing {
        setActive("ALWAYS")
        armored = true
        setMode("FILE")
        publicKey.set("public.pgp")
        secretKey.set("private.pgp")
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

//nexusPublishing {
//    repositories {
//       sonatype {
//           nexusUrl.set(uri(Meta.release))
//           snapshotRepositoryUrl.set(uri(Meta.snapshot))
//           val ossrhUsername = providers
//               .environmentVariable("OSSRH_USERNAME")
//               .forUseAtConfigurationTime()
//           val ossrhPassword = providers
//               .environmentVariable("OSSRH_PASSWORD")
//               .forUseAtConfigurationTime()
//           if (ossrhUsername.isPresent && ossrhPassword.isPresent) {
//               username.set(ossrhUsername.get())
//                password.set(ossrhPassword.get())
//            }
//        }
//    }
//}