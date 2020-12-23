import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    application
    id("com.google.protobuf") apply false
    kotlin("jvm") version "1.4.21"
}

group = "ru.itmo.chori"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

subprojects {
    apply {
        plugin("java")
        plugin("application")
        plugin("kotlin")
    }

    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        implementation("com.google.protobuf:protobuf-java:3.14.0")
        implementation("com.google.protobuf:protobuf-java-util:3.14.0")

        testImplementation(kotlin("test-junit"))
    }
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}