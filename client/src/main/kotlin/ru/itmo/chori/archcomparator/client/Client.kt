package ru.itmo.chori.archcomparator.client

import ru.itmo.chori.archcomparator.Message
import ru.itmo.chori.archcomparator.SERVER_PORT
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
        val outputStream = socket.getOutputStream()

        for (i in 1..messageCount) {
            val data = List(dataSize) { Random.nextInt() }
            val message = Message.newBuilder().addAllData(data).build()
            message.writeDelimitedTo(outputStream)

            Message.parseDelimitedFrom(inputStream)
            Thread.sleep(delayBeforeNextMessage.toMillis())
        }
    }
}

fun main() {
    runClient(100, Duration.ofMillis(100), 10)
}
