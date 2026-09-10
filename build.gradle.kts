plugins {
    kotlin("jvm") version "2.0.20"
    application
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass = "atelier.MainKt"
}
//for using custom source path
sourceSets.main {
    kotlin.srcDirs("src/main/atelier")
}