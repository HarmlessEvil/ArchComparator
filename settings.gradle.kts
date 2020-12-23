
rootProject.name = "ArchComparator"

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.13")
    }
}

include("shared", "server", "client")
