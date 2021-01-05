package ru.itmo.chori.archcomparator.server

import ru.itmo.chori.archcomparator.Message
import ru.itmo.chori.archcomparator.toByteBuffer
import java.io.ByteArrayInputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousCloseException
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.time.Duration
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

class AsynchronousServer(override val port: Int, override val threadPoolSize: Int) : ServerWithStatistics {
    private class Attachment(val clientId: Long) {
        val sizeBuffer: ByteBuffer = ByteBuffer.allocate(Int.SIZE_BYTES)
        lateinit var buffer: ByteBuffer
    }

    @Volatile
    private var isRunning = true

    private val threadPool = Executors.newFixedThreadPool(threadPoolSize) { runnable ->
        thread(start = false, name = "async server") { runnable.run() }
    }
    private val serverSocketChannel = AsynchronousServerSocketChannel.open().bind(InetSocketAddress(port))

    private val acceptor = thread(name = "acceptor") {
        var id: Long = 0
        while (isRunning) {
            val future = serverSocketChannel.accept()
            try {
                val connection = future.get()
                readMessageHeader(connection, Attachment(id++))
            } catch (e: ExecutionException) {
                if (e.cause is AsynchronousCloseException) {
                    break // isRunning will be false according to #close method
                }
            }
        }
    }

    private fun readMessageHeader(socketChannel: AsynchronousSocketChannel, attachment: Attachment) {
        socketChannel.read(attachment.sizeBuffer, attachment, object : CompletionHandler<Int, Attachment> {
            override fun completed(bytesRead: Int, attachment: Attachment) {
                if (bytesRead == -1) {
                    return // End of stream
                }

                if (attachment.sizeBuffer.hasRemaining()) {
                    readMessageHeader(socketChannel, attachment)
                    return
                }

                attachment.sizeBuffer.flip()
                attachment.buffer = ByteBuffer.allocate(attachment.sizeBuffer.int)

                readMessageBody(socketChannel, attachment)
            }

            override fun failed(exc: Throwable, attachment: Attachment) {
                exc.printStackTrace()
            }
        })
    }

    override val tasksTime: MutableMap<Long, MutableList<Duration>> = emptyMap<Long, MutableList<Duration>>()
        .toMutableMap()

    private fun readMessageBody(socketChannel: AsynchronousSocketChannel, attachment: Attachment) {
        socketChannel.read(attachment.buffer, attachment, object : CompletionHandler<Int, Attachment> {
            override fun completed(bytesRead: Int, attachment: Attachment) {
                if (bytesRead == -1) {
                    return // End of stream
                }

                if (attachment.buffer.hasRemaining()) {
                    readMessageBody(socketChannel, attachment)
                    return
                }

                attachment.buffer.flip()
                val bytes = ByteArray(attachment.buffer.remaining())
                attachment.buffer.get(bytes)

                val byteArrayInputStream = ByteArrayInputStream(bytes)
                val inputMessage = Message.parseFrom(byteArrayInputStream)

                if (!isRunning) {
                    return
                }

                threadPool.submit {
                    val data: List<Int>
                    val taskTime = measureTimeMillis {
                        data = task(inputMessage.dataList.toMutableList())
                    }

                    storeTaskTimeForClient(attachment.clientId, Duration.ofMillis(taskTime))
                    val outputMessage = Message.newBuilder().addAllData(data).build()

                    sendResponse(socketChannel, outputMessage.toByteBuffer(), attachment.clientId)
                }
            }

            override fun failed(exc: Throwable, attachment: Attachment) {
                exc.printStackTrace()
            }
        })
    }

    private fun sendResponse(socketChannel: AsynchronousSocketChannel, buffer: ByteBuffer, clientId: Long) {
        socketChannel.write(buffer, null, object : CompletionHandler<Int, Nothing?> {
            override fun completed(result: Int, attachment: Nothing?) {
                readMessageHeader(socketChannel, Attachment(clientId))
            }

            override fun failed(exc: Throwable, attachment: Nothing?) {
                exc.printStackTrace()
            }
        })
    }

    override fun close() {
        isRunning = false

        threadPool.shutdown()
        serverSocketChannel.close()

        acceptor.join()
    }
}

fun main() {
    val server = AsynchronousServer(port = 8080, threadPoolSize = 4)
    server.use {
        println("Accepting connections on localhost:${it.port}")
        println("Press ENTER to stop")

        readLine()
    }

    println(server.tasksTime)
}
