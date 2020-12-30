package ru.itmo.chori.archcomparator.server

import java.io.Closeable
import java.time.Duration

/**
 * Contract: server should start accepting connections after constructed. It should stop accepting it after close()
 * method is called. You shouldn't be able to start this instance again
 */
interface Server: Closeable {
    /**
     * For each client: time that consumed each task
     */
    val tasksTime: Map<Long, List<Duration>>

    // TODO: Uncomment when remember, how to compute it
//    val clientProcessTime: Map<Long, List<Duration>>

}

// Possible class cast exception left intentionally
@Suppress("unchecked_cast")
fun Server.storeTaskTimeForClient(clientId: Long, taskTime: Duration) {
    val timeMap = tasksTime as MutableMap<Long, MutableList<Duration>>

    timeMap.putIfAbsent(clientId, emptyList<Duration>().toMutableList())
    timeMap[clientId]?.add(taskTime)
}
