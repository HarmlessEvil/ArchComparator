package ru.itmo.chori.archcomparator.gui

import ru.itmo.chori.archcomparator.server.AsynchronousServer
import ru.itmo.chori.archcomparator.server.ManyThreadedServer
import ru.itmo.chori.archcomparator.server.NonBlockingServer
import ru.itmo.chori.archcomparator.server.ServerWithStatistics

fun serverFactory(architecture: Architecture, port: Int, threadPoolSize: Int): ServerWithStatistics {
    return when(architecture) {
        ManyThreadedArchitecture -> ManyThreadedServer(port, threadPoolSize)
        NonBlockingArchitecture -> NonBlockingServer(port, threadPoolSize)
        AsynchronousArchitecture -> AsynchronousServer(port, threadPoolSize)
    }
}
