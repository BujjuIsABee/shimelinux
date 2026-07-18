plugins {
    kotlin("jvm") version "2.3.21"
}

group = "io.github.bujjuisabee"
version = "1.1.3"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.dorkbox:SystemTray:4.4")
    implementation("com.formdev:flatlaf:3.7.1")
    implementation("com.github.hypfvieh:dbus-java-core:5.2.0")
    implementation("com.github.hypfvieh:dbus-java-transport-junixsocket:5.2.0")
    implementation("org.openjdk.nashorn:nashorn-core:15.7")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.14.11")
}

kotlin {
    jvmToolchain(21)
}

tasks.register("buildWaylandLib", Exec::class) {
    description = "Builds the Wayland library"

    isIgnoreExitValue = true

    workingDir = File("$projectDir/shimelinux_wayland")
    commandLine("cargo", "build", "--release")
}

tasks.register("copyWaylandLib", Copy::class) {
    description = "Copies the Wayland library to resources"

    from("$projectDir/shimelinux_wayland/target/release/libshimelinux_wayland.so")
    into(layout.buildDirectory.dir("wayland-lib"))
}

tasks.processResources {
    dependsOn("buildWaylandLib")
    dependsOn("copyWaylandLib")

    from(layout.buildDirectory.dir("wayland-lib")) {
        into("lib")
    }
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.group_finity.mascot.MainKt"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })

    exclude("META-INF/*.RSA", "META-INF/*.DSA", "META-INF/*.SF")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
}
