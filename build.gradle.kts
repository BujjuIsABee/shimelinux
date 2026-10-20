plugins {
    kotlin("jvm") version "2.4.21"
    id("com.github.gmazzo.buildconfig") version "6.1.2"
    id("io.github.jwharm.flatpak-gradle-generator") version "1.8.0"
}

group = "io.github.bujjuisabee"
version = "1.4.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.dorkbox:SystemTray:4.4")
    implementation("com.formdev:flatlaf:3.7.2")
    implementation("com.github.hypfvieh:dbus-java-core:5.2.2")
    implementation("com.github.hypfvieh:dbus-java-transport-junixsocket:5.2.2")
    implementation("org.openjdk.nashorn:nashorn-core:15.7")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.14.11")
}

kotlin {
    jvmToolchain(21)
}

buildConfig {
    buildConfigField("VERSION", provider { project.version.toString() })
    buildConfigField("TARGET_OS", provider { System.getProperty("os.name").orEmpty().lowercase() })
    buildConfigField("TARGET_ARCH", provider { System.getProperty("os.arch").orEmpty().lowercase() })
}

tasks.register("buildWaylandLib", Exec::class) {
    description = "Builds the Wayland library"

    workingDir = File("$projectDir/shimelinux_wayland")
    commandLine("cargo", "build", "--release")
}

tasks.register("copyWaylandLib", Copy::class) {
    dependsOn("buildWaylandLib")

    description = "Copies the Wayland library to resources"

    from("$projectDir/shimelinux_wayland/target/release/libshimelinux_wayland.so")
    into(layout.buildDirectory.dir("wayland-lib"))
}

tasks.register("cleanWaylandLib", Exec::class) {
    description = "Cleans the Wayland library project"

    workingDir = File("$projectDir/shimelinux_wayland")
    commandLine("cargo", "clean")
}

tasks.processResources {
    dependsOn("copyWaylandLib")

    from(layout.buildDirectory.dir("wayland-lib")) {
        into("lib")
    }
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.group_finity.mascot.Main"
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

tasks.clean {
    dependsOn("cleanWaylandLib")
}

tasks.flatpakGradleGenerator {
    outputFile = file("./gradle-sources.json")
    downloadDirectory = "./offline-repository"
}
