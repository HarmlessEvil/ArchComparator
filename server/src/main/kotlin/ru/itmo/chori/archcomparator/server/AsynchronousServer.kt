package ru.itmo.chori.archcomparator.server

import ru.itmo.chori.archcomparator.Message
import ru.itmo.chori.archcomparator.SERVER_PORT
import ru.itmo.chori.archcomparator.toByteBuffer
import java.io.ByteArrayInputStream
import java.io.Closeable
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousCloseException
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class AsynchronousServer : Closeable {
    private class Attachment {
        val sizeBuffer: ByteBuffer = ByteBuffer.allocate(Int.SIZE_BYTES)
        lateinit var buffer: ByteBuffer
    }

    @Volatile
    private var isRunning = true

    private val threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE)
    private val serverSocketChannel = AsynchronousServerSocketChannel.open().also {
        it.bind(InetSocketAddress(SERVER_PORT))
    }

    private val acceptor = thread {
        while (isRunning) {
            val future = serverSocketChannel.accept()
            try {
                val connection = future.get()
                readMessageHeader(connection, Attachment())
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

                threadPool.submit {
                    val data = task(inputMessage.dataList)
                    val outputMessage = Message.newBuilder().addAllData(data).build()

                    sendResponse(socketChannel, outputMessage.toByteBuffer())
                }
            }

            override fun failed(exc: Throwable, attachment: Attachment) {
                exc.printStackTrace()
            }
        })
    }

    private fun sendResponse(socketChannel: AsynchronousSocketChannel, buffer: ByteBuffer) {
        socketChannel.write(buffer, null, object : CompletionHandler<Int, Nothing?> {
            override fun completed(result: Int, attachment: Nothing?) {
                readMessageHeader(socketChannel, Attachment())
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
    AsynchronousServer().use {
        println("Accepting connections on localhost:$SERVER_PORT")
        println("Press ENTER to stop")

        readLine()
    }
}
