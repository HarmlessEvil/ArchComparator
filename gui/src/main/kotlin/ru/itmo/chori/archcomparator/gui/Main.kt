package ru.itmo.chori.archcomparator.gui

import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.stage.Stage
import tornadofx.*

const val WINDOW_WIDTH = 500.0

class Architecture(val name: String, val description: String) {
    override fun toString(): String {
        return name
    }
}

class MainView : View("Architecture Comparator") {
    private val architectures = FXCollections.observableArrayList(
        Architecture(
            "2 threads for each client",
            "Server creates a separate thread for receiving queries and a separate thread for sending " +
                    "responses for each client"
        ),
        Architecture(
            "Non-blocking",
            "Server has one thread with selector for receiving queries in non-blocking manner and one " +
                    "thread with selector for sending responses"
        ),
        Architecture(
            "Asynchronous",
            "All reads and writes on server are implemented in asynchronous manner: it has some thread " +
                    "pool, where id does all reads and writes. And after each (non-)successful operation, " +
                    "corresponding callback is called"
        )
    )
    private val selectedArchitecture = SimpleObjectProperty(architectures[0])
    private val selectedArchitectureDescription = selectedArchitecture.stringBinding { it?.description }

    override val root = borderpane {
        top {
            label("ArchComparator")
        }

        center {
            form {
                fieldset("Server architecture") {
                    field("Choose server architecture") {
                        combobox(selectedArchitecture, architectures)
                    }

                    text(selectedArchitectureDescription) {
                        style {
                            wrappingWidth = WINDOW_WIDTH - 20.0
                        }
                    }
                }
            }
        }
    }
}

class ArchitectureComparatorApp : App(MainView::class) {
    override fun start(stage: Stage) {
        with(stage) {
            maxWidth = WINDOW_WIDTH
            isResizable = false

            super.start(this)
        }
    }
}

fun main(args: Array<String>): Unit = launch<ArchitectureComparatorApp>(args)
