package ru.itmo.chori.archcomparator.server

import ru.itmo.chori.archcomparator.Message
import ru.itmo.chori.archcomparator.receiveMessageFrom
import ru.itmo.chori.archcomparator.sendTo
import java.io.DataOutputStream
import java.io.EOFException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.time.Duration
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

class ManyThreadedServer(override val port: Int, override val threadPoolSize: Int) : ServerWithStatistics {
    private val threadPool = Executors.newFixedThreadPool(threadPoolSize)

    @Volatile
    private var isRunning = true

    private val serverSocket = ServerSocket(port)
    private val acceptor = thread {
        var id: Long = 0
        while (isRunning) {
            try {
                acceptConnection(serverSocket.accept(), id++)
            } catch (e: SocketException) { // serverSocket.close() should throw this – will abort .accept()
                break
            }
        }
    }

    override val tasksTime: MutableMap<Long, MutableList<Duration>> = emptyMap<Long, MutableList<Duration>>()
        .toMutableMap()

    private fun acceptConnection(socket: Socket, id: Long) {
        thread {
            val sender = Executors.newSingleThreadExecutor()
            socket.use { socket ->
                val inputStream = socket.getInputStream()

                while (isRunning) {
                    try {
                        val message = receiveMessageFrom(inputStream)

                        threadPool.submit {
                            val data: List<Int>
                            val taskTime = measureTimeMillis {
                                data = task(message.dataList)
                            }

                            storeTaskTimeForClient(id, Duration.ofMillis(taskTime))

                            sender.submit { sendResponse(socket, Message.newBuilder().addAllData(data).build()) }
                        }
                    } catch (e: EOFException) { // will throw if all data consumed and client disconnected
                        break
                    }
                }
            }

            sender.shutdown()
        }
    }

    private fun sendResponse(socket: Socket, message: Message) {
        val outputStream = socket.getOutputStream()
        val dataOutputStream = DataOutputStream(outputStream)

        message sendTo dataOutputStream
    }

    /**
     * After this operation client threads still might keep working for some time, but eventually will finish in future
     */
    override fun close() {
        isRunning = false
        serverSocket.close()

        threadPool.shutdown()
        acceptor.join()
    }
}

fun main() {
    val server = ManyThreadedServer(port = 8080, threadPoolSize = 4)
    server.use {
        println("Accepting connections on localhost:${it.port}")
        println("Press ENTER to stop")

        readLine()
    }

    println(server.tasksTime)
}
