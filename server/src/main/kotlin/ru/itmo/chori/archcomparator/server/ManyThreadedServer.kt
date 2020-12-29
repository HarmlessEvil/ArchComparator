package ru.itmo.chori.archcomparator.server

import ru.itmo.chori.archcomparator.Message
import ru.itmo.chori.archcomparator.SERVER_PORT
import java.io.*
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
    private val acceptor = thread {
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
            val sender = Executors.newSingleThreadExecutor()
            socket.use { socket ->
                val inputStream = socket.getInputStream()
                val dataInputStream = DataInputStream(inputStream)

                while (isRunning) {
                    try {
                        dataInputStream.readInt()
                        val message = Message.parseDelimitedFrom(inputStream)

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
        }
    }

    private fun sendResponse(socket: Socket, message: Message) {
        val outputStream = socket.getOutputStream()
        val dataOutputStream = DataOutputStream(outputStream)
        val byteArrayOutputStream = ByteArrayOutputStream()
        message.writeDelimitedTo(byteArrayOutputStream)

        dataOutputStream.writeInt(byteArrayOutputStream.size())
        dataOutputStream.write(byteArrayOutputStream.toByteArray())
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
