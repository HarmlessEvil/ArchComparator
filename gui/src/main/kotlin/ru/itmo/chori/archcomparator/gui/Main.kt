package ru.itmo.chori.archcomparator.gui

import javafx.stage.Stage
import tornadofx.*

const val WINDOW_WIDTH = 500.0

class MainView : View("Architecture Comparator") {
    private val settings = RunSettings()

    override val root = borderpane {
        top {
            label("ArchComparator")
        }

        center {
            form {
                fieldset("Server architecture") {
                    field("Choose server architecture") {
                        combobox(settings.selectedArchitecture, settings.architectures)
                    }

                    text(settings.selectedArchitectureDescription) {
                        style {
                            wrappingWidth = WINDOW_WIDTH - 20.0
                        }
                    }
                }

                fieldset("Client settings") {
                    field("Number of queries per client (X)") {
                        textfield(settings.queriesPerClient) {
                            filterInput {
                                it.controlNewText.isInt()
                            }
                        }
                    }

                    field("Testing parameter") {
                        combobox(settings.selectedTestingParameter, settings.testingParameters)
                    }

                    text(settings.selectedTestingParameterDescription) {
                        style {
                            wrappingWidth = WINDOW_WIDTH - 20.0
                        }
                    }
                }

                fieldset("Parameter configuration") {
                    field("Min value") {
                        textfield(settings.minParameterValue) {
                            filterInput {
                                it.controlNewText.isInt()
                            }
                        }
                    }

                    field("Max value") {
                        textfield(settings.maxParameterValue) {
                            filterInput {
                                it.controlNewText.isInt()
                            }
                        }
                    }

                    field("Step") {
                        textfield(settings.parameterStep) {
                            filterInput {
                                it.controlNewText.isInt()
                            }
                        }
                    }
                }

                fieldset("Other settings") {
                    field("Server port") {
                        // format %d removes space as triplets separator, e.g. '8 080'
                        textfield(settings.serverPort.asString("%d")) {
                            filterInput {
                                it.controlNewText.isInt()
                            }
                        }
                    }

                    field("Server thread pool size") {
                        textfield(settings.serverThreadPoolSize) {
                            filterInput {
                                it.controlNewText.isInt()
                            }
                        }
                    }
                }

                // TODO: Validate
                // TODO: Run!
            }
        }
    }
}

class ArchitectureComparatorApp : App(MainView::class) {
    override fun start(stage: Stage) {
        with(stage) {
            maxWidth = WINDOW_WIDTH
            minWidth = WINDOW_WIDTH

            super.start(this)
        }
    }
}

fun main(args: Array<String>): Unit = launch<ArchitectureComparatorApp>(args)
