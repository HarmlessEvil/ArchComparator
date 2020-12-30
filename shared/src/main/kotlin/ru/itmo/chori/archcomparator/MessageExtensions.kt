package ru.itmo.chori.archcomparator

import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream

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
