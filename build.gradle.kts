import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.4.4"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.spring") version "1.4.30"
    kotlin("kapt") version "1.4.30"
    kotlin("plugin.serialization") version "1.4.30"
}

group = "uk.me.danielharman"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

val developmentOnly by configurations.creating
configurations {
    runtimeClasspath {
        extendsFrom(developmentOnly)
    }
}

repositories {
    mavenCentral()
    maven {
        name = "m2-dv8tion"
        url = uri("https://m2.dv8tion.net/releases")
    }
}

dependencies {
    implementation(group="org.kohsuke", name="wordnet-random-name", version= "1.3")
    implementation(group = "joda-time", name = "joda-time", version = "2.10.6")
    implementation("net.dv8tion:JDA:4.2.1_253")
    implementation("com.sedmelluq:lavaplayer:1.3.75")
    implementation(group = "com.typesafe.akka", name = "akka-actor_2.13", version = "2.6.10")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:1.0-M1-1.4.0-rc") // JVM dependency
    implementation(group="io.ktor", name="ktor-client-cio", version="1.4.0")
    implementation("me.xdrop:fuzzywuzzy:1.3.1")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.0.5") // for kotest framework
    testImplementation ("io.kotest:kotest-assertions-core-jvm:4.0.5" )// for kotest core jvm assertions
    testImplementation ("io.kotest:kotest-property-jvm:4.0.5")// for kotest property test
    testImplementation("io.mockk:mockk:1.10.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}
