package ru.itmo.chori.archcomparator.server

import ru.itmo.chori.archcomparator.Message
import ru.itmo.chori.archcomparator.SERVER_PORT
import ru.itmo.chori.archcomparator.toByteBuffer
import java.io.ByteArrayInputStream
import java.io.Closeable
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.*
import java.time.Duration
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

class NonBlockingServer : Server {
    private class Attachment(val clientId: Long) {
        enum class State {
            ReadingSize,
            ReadingMessage
        }

        var state = State.ReadingSize

        val sizeBuffer: ByteBuffer = ByteBuffer.allocate(Int.SIZE_BYTES)
        lateinit var buffer: ByteBuffer

        val queue = ConcurrentLinkedQueue<ByteBuffer>()
    }

    private val threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE)

    @Volatile
    private var isRunning = true

    private val serverSocketChannel = ServerSocketChannel.open().also {
        it.bind(InetSocketAddress(SERVER_PORT))
    }
    private val readSelector = Selector.open()
    private val writeSelector = Selector.open()

    private val acceptor = thread {
        var id: Long = 0
        while (isRunning) {
            try {
                serverSocketChannel.accept().also {
                    it.configureBlocking(false)

                    val attachment = Attachment(id++)
                    it.register(readSelector, SelectionKey.OP_READ, attachment)
                    readSelector.wakeup()

                    it.register(writeSelector, SelectionKey.OP_WRITE, attachment)
                    writeSelector.wakeup()
                }
            } catch (e: AsynchronousCloseException) { // Channel will be closed in another thread
                break
            }
        }
    }

    override val tasksTime: MutableMap<Long, MutableList<Duration>> = emptyMap<Long, MutableList<Duration>>()
        .toMutableMap()

    private val reader = thread {
        while (isRunning) {
            try {
                readSelector.select { key ->
                    val channel = key.channel() as SocketChannel
                    val attachment = key.attachment() as Attachment

                    // Don't need to check, whether key.isReadable as it is the only interesting operation
                    when (attachment.state) {
                        Attachment.State.ReadingSize -> {
                            val bytesRead = channel.read(attachment.sizeBuffer)
                            if (bytesRead == -1) {
                                channel.close() // Should be enough: https://stackoverflow.com/a/24576864/12411158
                                return@select
                            }

                            if (attachment.sizeBuffer.hasRemaining()) {
                                return@select
                            }

                            attachment.state = Attachment.State.ReadingMessage

                            attachment.sizeBuffer.flip()
                            attachment.buffer = ByteBuffer.allocate(attachment.sizeBuffer.int)
                        }

                        Attachment.State.ReadingMessage -> {
                            val bytesRead = channel.read(attachment.buffer)
                            if (bytesRead == -1) {
                                channel.close()
                                return@select
                            }

                            if (attachment.buffer.hasRemaining()) {
                                return@select
                            }

                            attachment.buffer.flip()

                            val inputMessageBytes = ByteArray(attachment.buffer.remaining()) // == size
                            attachment.buffer.get(inputMessageBytes)

                            val byteArrayInputStream = ByteArrayInputStream(inputMessageBytes)
                            val inputMessage = Message.parseFrom(byteArrayInputStream)

                            threadPool.submit {
                                val data: List<Int>
                                val taskTime = measureTimeMillis {
                                    data = task(inputMessage.dataList)
                                }

                                storeTaskTimeForClient(attachment.clientId, Duration.ofMillis(taskTime))
                                val outputMessage = Message.newBuilder().addAllData(data).build()

                                attachment.queue.add(outputMessage.toByteBuffer())
                            }

                            attachment.sizeBuffer.clear()
                            attachment.buffer.clear()
                            attachment.state = Attachment.State.ReadingSize
                        }
                    }
                }
            } catch (e: ClosedSelectorException) {
                break
            }
        }
    }

    private val writer = thread {
        while (isRunning) {
            try {
                writeSelector.select { key ->
                    val channel = key.channel() as SocketChannel
                    val attachment = key.attachment() as Attachment

                    val buffer = attachment.queue.peek() ?: return@select
                    channel.write(buffer)

                    if (buffer.hasRemaining()) {
                        return@select
                    }

                    attachment.queue.poll()
                }
            } catch (e: ClosedSelectorException) {
                break
            }
        }
    }

    override fun close() {
        isRunning = false
        serverSocketChannel.close()

        threadPool.shutdown()

        acceptor.join()

        readSelector.close()
        reader.join()

        writeSelector.close()
        writer.join()
    }
}

fun main() {
    val server = NonBlockingServer()
    server.use {
        println("Accepting connections on localhost:$SERVER_PORT")
        println("Press ENTER to stop")

        readLine()
    }

    println(server.tasksTime)
}
