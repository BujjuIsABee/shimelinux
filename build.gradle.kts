plugins {
    kotlin("jvm") version "2.3.21"
}

group = "io.github.bujjuisabee"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.openjdk.nashorn:nashorn-core:15.7")
    implementation("com.dorkbox:SystemTray:4.5")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}