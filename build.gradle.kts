plugins {
    kotlin("jvm") version "1.9.21"
    id("io.papermc.paperweight.userdev") version "1.5.10"
}

group = "io.github.koba"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")
    implementation("io.github.monun:kommand-api:latest.release")
    //    implementation("io.github.monun:tap-api:latest.release")
    //    implementation("io.github.monun:invfx-api:latest.release")
    //    implementation("io.github.monun:heartbeat-coroutines:latest.release")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}