plugins {
    `java-library`
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jetbrains.kotlin.jvm") version "2.2.10"
    id("org.jetbrains.kotlin.plugin.spring") version "2.2.10"
    id("org.jetbrains.kotlin.kapt") version "2.2.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10"
    id("org.barfuin.gradle.jacocolog") version "3.1.0"
    jacoco
}

group = "uk.me.danielharman"

version = "Kobot"

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

springBoot { buildInfo() }

jacoco { toolVersion = "0.8.13" }

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
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb:3.5.4")
    implementation("org.springframework.boot:spring-boot-starter-web:3.5.4")
    implementation("org.springframework.boot:spring-boot-starter-actuator:3.5.4")
    implementation("org.springframework.boot:spring-boot-starter-amqp:3.5.4")
    developmentOnly("org.springframework.boot:spring-boot-devtools:3.5.4")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:3.5.4")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.5.4") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    implementation("net.dv8tion:JDA:5.6.1")
    implementation("dev.arbjerg:lavaplayer:2.2.4")
    implementation("dev.lavalink.youtube:common:1.13.4")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.2.10")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.2.10")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("io.ktor:ktor-client-cio:3.2.3")
    implementation("me.xdrop:fuzzywuzzy:1.4.0")
    implementation("org.apache.commons:commons-lang3:3.19.0")
    implementation("commons-io:commons-io:2.20.0")
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:4.21.0")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.9.1") // for kotest framework
    testImplementation(
        "io.kotest:kotest-assertions-core-jvm:5.9.1",
    ) // for kotest core jvm assertions
    testImplementation("io.kotest:kotest-property-jvm:5.9.1") // for kotest property test
    testImplementation(group = "org.mockito", name = "mockito-core", version = "5.19.0")
    testImplementation("org.hamcrest:hamcrest:3.0")
}

configurations { runtimeOnly { exclude(group = "commons-logging", module = "commons-logging") } }

tasks.withType<Test> { useJUnitPlatform() }

tasks.getByName<Jar>("jar") { enabled = false }

kotlin { compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict") } }
