package ru.itmo.chori.archcomparator.client

import ru.itmo.chori.archcomparator.Message
import ru.itmo.chori.archcomparator.receiveMessageFrom
import ru.itmo.chori.archcomparator.sendTo
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.Socket
import java.time.Duration
import kotlin.random.Random
import kotlin.system.measureTimeMillis

// N is dataSize
// 𝚫 is delayBeforeNextMessage
// X is messageCount

// Client is stateless and can only perform one action, so it doesn't have to be a class
fun runClient(serverPort: Int, dataSize: Int, delayBeforeNextMessage: Duration, messageCount: Int) {
    Socket(InetAddress.getLocalHost(), serverPort).use { socket ->
        val inputStream = socket.getInputStream()

        val outputStream = socket.getOutputStream()
        val dataOutputStream = DataOutputStream(outputStream)

        repeat(messageCount) {
            val data = List(dataSize) { Random.nextInt() }
            val message = Message.newBuilder().addAllData(data).build()

            message sendTo dataOutputStream
            receiveMessageFrom(inputStream)

            Thread.sleep(delayBeforeNextMessage.toMillis())
        }
    }
}

fun runClientAndMeasureTime(
    serverPort: Int,
    dataSize: Int,
    delayBeforeNextMessage: Duration,
    messageCount: Int
): Duration {
    val totalTime = measureTimeMillis {
        runClient(serverPort, dataSize, delayBeforeNextMessage, messageCount)
    }

    return Duration.ofMillis(totalTime / messageCount)
}

fun main() {
    println(runClientAndMeasureTime(8080, 5000, Duration.ofMillis(100), 10))
}
