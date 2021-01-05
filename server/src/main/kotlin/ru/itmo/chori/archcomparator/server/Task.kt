package ru.itmo.chori.archcomparator.server

fun task(data: MutableList<Int>): List<Int> {
    bubbleSort(data)

    return data
}

fun bubbleSort(data: MutableList<Int>) {
    var swap = true
    while (swap) {
        swap = false

        for (i in 0 until data.size - 1) {
            if (data[i] > data[i + 1]) {
                val temp = data[i]

                data[i] = data[i + 1]
                data[i + 1] = temp

                swap = true
            }
        }
    }
}
