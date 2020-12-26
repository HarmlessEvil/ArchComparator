package ru.itmo.chori.archcomparator.server

import ru.itmo.chori.archcomparator.Message
import ru.itmo.chori.archcomparator.SERVER_PORT
import java.io.Closeable
import java.io.EOFException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class ManyThreadedServer : Closeable {
    private val threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE)

    @Volatile
    private var isRunning = true

    private val serverSocket = ServerSocket(SERVER_PORT)
    private val acceptor: Thread = thread {
        while (isRunning) {
            try {
                acceptConnection(serverSocket.accept())
            } catch (e: SocketException) { // serverSocket.close() should throw this – will abort .accept()
                break
            }
        }
    }

    private fun acceptConnection(socket: Socket) {
        thread {
            try {
                val sender = Executors.newSingleThreadExecutor()
                socket.use { socket ->
                    val inputStream = socket.getInputStream()

                    while (isRunning) {
                        try {
                            // parseDelimitedFrom may return null after attempt to read from closed stream
                            val message = Message.parseDelimitedFrom(inputStream) ?: break

                            threadPool.submit {
                                val data = task(message.dataList)
                                sender.submit { sendResponse(socket, Message.newBuilder().addAllData(data).build()) }
                            }
                        } catch (e: EOFException) { // will throw if all data consumed and client disconnected
                            break
                        }
                    }
                }

                sender.shutdown()
            } catch (e: Exception) {
                e.printStackTrace()

                isRunning = false
            }
        }
    }

    private fun sendResponse(socket: Socket, message: Message) {
        val outputStream = socket.getOutputStream()
        message.writeDelimitedTo(outputStream)
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
    ManyThreadedServer().use {
        println("Accepting connections on localhost:$SERVER_PORT")
        println("Press ENTER to stop")

        readLine()
    }
}
