package ru.itmo.chori.archcomparator.server

import java.util.*
import java.util.concurrent.Callable

class Task(private val data: IntArray): Callable<IntArray> {
    override fun call(): IntArray {
        Arrays.sort(data)
        return data
    }
}
