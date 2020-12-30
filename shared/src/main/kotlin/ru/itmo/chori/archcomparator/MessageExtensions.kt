package ru.itmo.chori.archcomparator

import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.nio.ByteBuffer

infix fun Message.sendTo(stream: DataOutputStream) {
    val byteArrayOutputStream = ByteArrayOutputStream()
    writeTo(byteArrayOutputStream)

    stream.writeInt(byteArrayOutputStream.size())
    stream.write(byteArrayOutputStream.toByteArray())
}

fun receiveMessageFrom(stream: InputStream): Message {
    val dataInputStream = DataInputStream(stream)
    val size = dataInputStream.readInt()
    val bytes = ByteArray(size)

    stream.read(bytes)
    return Message.parseFrom(bytes)
}

fun Message.toByteBuffer(): ByteBuffer {
    val byteArrayOutputStream = ByteArrayOutputStream()
    writeTo(byteArrayOutputStream)

    return ByteBuffer
        .allocate(Int.SIZE_BYTES + byteArrayOutputStream.size())
        .putInt(byteArrayOutputStream.size())
        .put(byteArrayOutputStream.toByteArray())
        .flip()
}