import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.openjfx.javafxplugin") version "0.0.9"
}

javafx {
    version = "12"
    modules = listOf("javafx.controls")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = JavaVersion.VERSION_11.toString()
}

dependencies {
    implementation(project(":server"))
    implementation(project(":client"))

    implementation("no.tornado:tornadofx:1.7.20")
}
