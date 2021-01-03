package ru.itmo.chori.archcomparator.gui

sealed class TestingParameter(
    val name: String,
    val variable: String,
    val description: String,
    val allowsZero: Boolean = false
) {
    override fun toString() = "$name ($variable)"
}
internal object ArraySize: TestingParameter(
    "Array size",
    "N",
    "Amount of numbers in array to be sent"
)
internal object ClientsCount: TestingParameter(
    "Amount of clients",
    "M",
    "Amount of simultaneously working clients. Each client will be started as a task in cached thread " +
            "pool, which effectively should be like if everyone have it's own thread, although giving the " +
            "possibility to avoid some unnecessary thread restarts"
)
internal object ClientDelay: TestingParameter(
    "Client delay",
    "Δ",
    "After client receives response from the server, it will wait Δ ms before sending the next " +
            "message",
    allowsZero = true
)
