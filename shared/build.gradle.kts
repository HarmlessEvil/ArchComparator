import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

apply {
    plugin("com.google.protobuf")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.14.0"
    }

    generatedFilesBaseDir = "$projectDir/src/"
}
