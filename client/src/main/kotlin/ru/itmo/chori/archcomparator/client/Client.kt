package ru.itmo.chori.archcomparator.client

import ru.itmo.chori.archcomparator.Message
import ru.itmo.chori.archcomparator.SERVER_PORT
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.Socket
import java.time.Duration
import kotlin.random.Random

// N is dataSize
// 𝚫 is delayBeforeNextMessage
// X is messageCount

// Client is stateless and can only perform one action, so it doesn't have to be a class
fun runClient(dataSize: Int, delayBeforeNextMessage: Duration, messageCount: Int) {
    Socket(InetAddress.getLocalHost(), SERVER_PORT).use { socket ->
        val inputStream = socket.getInputStream()
        val dataInputStream = DataInputStream(inputStream)

        val outputStream = socket.getOutputStream()
        val dataOutputStream = DataOutputStream(outputStream)

        for (i in 1..messageCount) {
            val data = List(dataSize) { Random.nextInt() }
            val message = Message.newBuilder().addAllData(data).build()
            val byteArrayOutputStream = ByteArrayOutputStream()
            message.writeDelimitedTo(byteArrayOutputStream)

            dataOutputStream.writeInt(byteArrayOutputStream.size())
            dataOutputStream.write(byteArrayOutputStream.toByteArray())

            dataInputStream.readInt() // Reads and discards message size
            Message.parseDelimitedFrom(inputStream)

            Thread.sleep(delayBeforeNextMessage.toMillis())
        }
    }
}

fun main() {
    runClient(100, Duration.ofMillis(100), 10)
}
