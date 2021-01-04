package ru.itmo.chori.archcomparator.gui

import ru.itmo.chori.archcomparator.server.*

fun serverFactory(architecture: Architecture, port: Int, threadPoolSize: Int): ServerWithStatistics {
    return when(architecture) {
        ManyThreadedArchitecture -> ManyThreadedServer(port, threadPoolSize)
        NonBlockingArchitecture -> NonBlockingServer(port, threadPoolSize)
        AsynchronousArchitecture -> AsynchronousServer(port, threadPoolSize)
    }
}
