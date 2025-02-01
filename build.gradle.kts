import me.qoomon.gradle.gitversioning.GitVersioningPluginConfig
import me.qoomon.gradle.gitversioning.GitVersioningPluginConfig.*

plugins {
    `java-library`
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"
    kotlin("kapt") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("me.qoomon.git-versioning") version "4.3.0"
    id("org.barfuin.gradle.jacocolog") version "3.1.0"
    jacoco
}

group = "uk.me.danielharman"

version = "Kobot"

gitVersioning.apply(closureOf<GitVersioningPluginConfig> {
    tag(closureOf<VersionDescription>{
        versionFormat = "\${version} \${tag}"
    })
    branch(closureOf<VersionDescription>{
        versionFormat = "\${version} \${branch}.\${commit.short}.\${commit.timestamp.datetime}"
    })
})

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

springBoot {
    buildInfo()
}

jacoco {
    toolVersion = "0.8.12"
}

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
    implementation("net.dv8tion:JDA:5.2.2")
    implementation("dev.arbjerg:lavaplayer:2.2.2")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb:3.4.1")
    implementation("org.springframework.boot:spring-boot-starter-web:3.4.1")
    implementation("org.springframework.boot:spring-boot-starter-actuator:3.4.1")
    implementation("dev.lavalink.youtube:common:1.11.4")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("io.ktor:ktor-client-cio:3.0.3")
    implementation("me.xdrop:fuzzywuzzy:1.4.0")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    developmentOnly("org.springframework.boot:spring-boot-devtools:3.4.1")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:3.4.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.4.1") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:4.18.1")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.9.1") // for kotest framework
    testImplementation ("io.kotest:kotest-assertions-core-jvm:5.9.1")// for kotest core jvm assertions
    testImplementation ("io.kotest:kotest-property-jvm:5.9.1")// for kotest property test
    testImplementation (group="org.mockito", name="mockito-core", version="5.15.2")
    testImplementation("org.hamcrest:hamcrest:3.0")
}

configurations {
    runtimeOnly {
        exclude(group = "commons-logging", module = "commons-logging")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}
