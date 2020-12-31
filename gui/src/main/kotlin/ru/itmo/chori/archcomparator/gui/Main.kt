package ru.itmo.chori.archcomparator.gui

import tornadofx.*

class MainView : View("Test view") {
    override val root = borderpane {
        center {
            label("Hello, world!")
        }
    }
}

class ArchitectureComparatorApp : App(MainView::class)

fun main(args: Array<String>): Unit = launch<ArchitectureComparatorApp>(args)
