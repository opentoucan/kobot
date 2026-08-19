plugins {
    `java-library`
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jetbrains.kotlin.jvm") version "2.3.10"
    id("org.jetbrains.kotlin.plugin.spring") version "2.3.10"
    id("org.jetbrains.kotlin.kapt") version "2.3.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.10"
    id("org.barfuin.gradle.jacocolog") version "4.0.1"
    jacoco
}

group = "uk.me.danielharman"

version = "Kobot"

springBoot { buildInfo() }

jacoco { toolVersion = "0.8.14" }

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
    }
}

repositories {
    mavenCentral()
    maven(url = "https://maven.lavalink.dev/releases")
}

dependencies {
    implementation("org.springframework:spring-jcl:6.2.16")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb:4.0.3")
    implementation("org.springframework.boot:spring-boot-starter-web:4.0.3")
    implementation("org.springframework.boot:spring-boot-starter-actuator:4.0.3")
    implementation("org.springframework.boot:spring-boot-starter-amqp:4.0.3")
    developmentOnly("org.springframework.boot:spring-boot-devtools:4.0.3")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:4.0.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test:4.0.3") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    implementation("net.dv8tion:JDA:6.3.1")
    implementation("dev.arbjerg:lavaplayer:2.2.6")

    // Interface to use for libraries
    implementation("club.minnced:jdave-api:0.1.7")

    // Compiled natives for libdave for the specified platform
    implementation("club.minnced:jdave-native-linux-x86-64:0.1.8")
    implementation("club.minnced:jdave-native-win-x86-64:0.1.7")

    implementation("dev.lavalink.youtube:common:1.18.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.21.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.3.10")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.3.10")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
    implementation("io.ktor:ktor-client-cio:3.4.1")
    implementation("me.xdrop:fuzzywuzzy:1.4.0")
    implementation("org.apache.commons:commons-lang3:3.20.0")
    implementation("commons-io:commons-io:2.21.0")
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:4.24.0")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:6.1.4") // for kotest framework
    testImplementation(
        "io.kotest:kotest-assertions-core-jvm:6.1.4",
    ) // for kotest core jvm assertions
    testImplementation("io.kotest:kotest-property-jvm:6.1.4") // for kotest property test
    testImplementation("org.mockito:mockito-core:5.22.0")
    testImplementation("org.hamcrest:hamcrest:3.0")
}

configurations { runtimeOnly { exclude(group = "commons-logging", module = "commons-logging") } }

tasks.withType<Test> { useJUnitPlatform() }

tasks.getByName<Jar>("jar") { enabled = false }

kotlin {
    jvmToolchain(25)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}
