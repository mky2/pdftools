plugins {
    kotlin("jvm") version "1.9.23"
    application
}

group = "com.github.mky2"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.apache.pdfbox:pdfbox:3.0.2")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

application {
    mainClass = "com.github.mky2.MainKt"
    val runDir = File("run/")
    runDir.mkdirs()
    tasks.run.get().workingDir = runDir
}