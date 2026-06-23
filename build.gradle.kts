plugins {
    kotlin("jvm") version "2.3.21"
}

group = "io.github.bujjuisabee"
version = "1.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.openjdk.nashorn:nashorn-core:15.7")
    implementation("com.dorkbox:SystemTray:4.4")
    implementation("com.github.hypfvieh:dbus-java-core:5.2.0")
    implementation("com.github.hypfvieh:dbus-java-transport-junixsocket:5.2.0")
    implementation("com.formdev:flatlaf:3.7.1")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.group_finity.mascot.MainKt"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from (sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })

    exclude("META-INF/*.RSA", "META-INF/*.DSA", "META-INF/*.SF")
}

tasks.test {
    useJUnitPlatform()
}