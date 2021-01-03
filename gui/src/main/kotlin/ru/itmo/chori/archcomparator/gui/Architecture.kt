package ru.itmo.chori.archcomparator.gui

sealed class Architecture(val name: String, val description: String) {
    override fun toString() = name
}
internal object ManyThreadedArchitecture: Architecture(
    "2 threads for each client",
    "Server creates a separate thread for receiving queries and a separate thread for sending " +
            "responses for each client"
)
internal object NonBlockingArchitecture: Architecture(
    "Non-blocking",
    "Server has one thread with selector for receiving queries in non-blocking manner and one " +
            "thread with selector for sending responses"
)
internal object AsynchronousArchitecture: Architecture(
    "Asynchronous",
    "All reads and writes on server are implemented in asynchronous manner: it has some thread " +
            "pool, where it does all reads and writes. And after each (non-)successful operation, " +
            "corresponding callback is called"
)
