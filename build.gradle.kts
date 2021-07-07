import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import me.qoomon.gradle.gitversioning.GitVersioningPluginConfig
import me.qoomon.gradle.gitversioning.GitVersioningPluginConfig.*

plugins {
    id("org.springframework.boot") version "2.5.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.5.20"
    kotlin("plugin.spring") version "1.5.20"
    kotlin("kapt") version "1.5.20"
    kotlin("plugin.serialization") version "1.5.20"
    id("me.qoomon.git-versioning") version "4.2.1"
    id("org.barfuin.gradle.jacocolog") version "2.0.0"
    jacoco
}

group = "uk.me.danielharman"
java.sourceCompatibility = JavaVersion.VERSION_16

version = "Kobot"
gitVersioning.apply(closureOf<GitVersioningPluginConfig> {
    tag(closureOf<VersionDescription>{
        versionFormat = "\${version} \${tag}"
    })
    branch(closureOf<VersionDescription>{
        versionFormat = "\${version} \${branch}.\${commit.short}.\${commit.timestamp.datetime}"
    })
})

springBoot {
    buildInfo()
}

jacoco {
    toolVersion = "0.8.7"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}
tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.isEnabled = true
        csv.isEnabled = false
        html.isEnabled = true
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
    implementation(group="org.kohsuke", name="wordnet-random-name", version= "1.5")
    implementation(group = "joda-time", name = "joda-time", version = "2.10.10")
    implementation(group="com.fasterxml.jackson.datatype", name="jackson-datatype-joda", version="2.12.4")
    implementation("net.dv8tion:JDA:4.3.0_294")
    implementation("com.sedmelluq:lavaplayer:1.3.77")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:1.0-M1-1.4.0-rc") // JVM dependency
    implementation(group="io.ktor", name="ktor-client-cio", version="1.6.1")
    implementation("me.xdrop:fuzzywuzzy:1.3.1")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.6.0") // for kotest framework
    testImplementation ("io.kotest:kotest-assertions-core-jvm:4.6.0" )// for kotest core jvm assertions
    testImplementation ("io.kotest:kotest-property-jvm:4.6.0")// for kotest property test
    testImplementation (group="org.mockito", name="mockito-core", version="3.11.2")
    testImplementation (group="org.mockito", name="mockito-inline", version="3.11.2")
    testImplementation(group="org.hamcrest", name="hamcrest-all", version="1.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "16"
    }
}
