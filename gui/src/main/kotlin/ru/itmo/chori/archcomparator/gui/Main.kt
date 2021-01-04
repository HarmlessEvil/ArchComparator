package ru.itmo.chori.archcomparator.gui

import javafx.scene.control.TextFormatter
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.stage.Stage
import javafx.util.converter.NumberStringConverter
import tornadofx.*

const val WINDOW_WIDTH = 500.0
const val WINDOW_HEIGHT = 900.0

class MainView : View("Architecture Comparator") {
    private val settings: RunSettingsModel by inject()

    override val root = borderpane {
        top {
            imageview("/logo.png") {
                isPreserveRatio = true
                fitWidth = WINDOW_WIDTH
            }
        }

        center {
            form {
                fieldset("Server architecture") {
                    field("Choose server architecture") {
                        combobox(settings.architecture, RunSettings.architectures)
                    }

                    text(settings.architecture.stringBinding { it?.description }) {
                        style {
                            wrappingWidth = WINDOW_WIDTH - 20.0
                        }
                    }
                }

                fieldset("Client settings") {
                    field("Number of queries per client (X)") {
                        textfield(settings.queriesPerClient) {
                            stripNonNumeric("")
                            textFormatter = TextFormatter(
                                NumberStringConverter("########"),
                                settings.queriesPerClient.value
                            )

                            validator {
                                if (it.isNullOrBlank())
                                    return@validator error("This field is required")

                                if (it.toInt() <= 0)
                                    error("Number of clients should be greater than zero")
                                else
                                    null
                            }
                        }
                    }

                    field("Testing parameter") {
                        combobox(settings.testingParameter, RunSettings.testingParameters)
                    }

                    text(settings.testingParameter.stringBinding { it?.description }) {
                        style {
                            wrappingWidth = WINDOW_WIDTH - 20.0
                        }
                    }
                }

                fieldset("Parameter configuration") {
                    field("Min value") {
                        textfield(settings.minParameterValue) {
                            stripNonNumeric("")
                            textFormatter = TextFormatter(
                                NumberStringConverter("########"),
                                settings.minParameterValue.value
                            )

                            validator {
                                if (it.isNullOrBlank())
                                    return@validator error("This field is required")

                                val name = "Minimal value of ${settings.testingParameter.value}"
                                val intValue = it.toInt()

                                if (settings.testingParameter.value.allowsZero) {
                                    if (intValue < 0)
                                        return@validator error("$name should be greater or equals to zero")
                                } else {
                                    if (intValue <= 0)
                                        return@validator error("$name should be greater than zero")
                                }

                                if (intValue >= settings.maxParameterValue.value)
                                    error("$name should be less than it's max value")
                                else
                                    null
                            }
                        }
                    }

                    field("Max value") {
                        textfield(settings.maxParameterValue) {
                            stripNonNumeric("")
                            textFormatter = TextFormatter(
                                NumberStringConverter("########"),
                                settings.maxParameterValue.value
                            )

                            validator {
                                if (it.isNullOrBlank())
                                    return@validator error("This field is required")

                                val name = "Maximal value of ${settings.testingParameter.value}"
                                val intValue = it.toInt()

                                if (settings.testingParameter.value.allowsZero) {
                                    if (intValue < 0)
                                        return@validator error("$name should be greater or equals to zero")
                                } else {
                                    if (intValue <= 0)
                                        return@validator error("$name should be greater than zero")
                                }

                                if (intValue <= settings.minParameterValue.value)
                                    error("$name should be greater than it's min value")
                                else
                                    null
                            }
                        }
                    }

                    field("Step") {
                        textfield(settings.parameterStep) {
                            stripNonNumeric("")
                            textFormatter = TextFormatter(
                                NumberStringConverter("########"),
                                settings.parameterStep.value
                            )

                            validator {
                                if (it.isNullOrBlank())
                                    return@validator error("This field is required")

                                if (it.toInt() <= 0)
                                    error("Step should be greater than zero")
                                else
                                    null
                            }
                        }
                    }
                }

                fieldset("Constant parameters") {
                    field(ArraySize.toString()) {
                        textfield(settings.arraySize) {
                            stripNonNumeric("")
                            textFormatter = TextFormatter(
                                NumberStringConverter("########"),
                                settings.arraySize.value
                            )

                            validator {
                                if (it.isNullOrBlank())
                                    return@validator error("This field is required")

                                if (it.toInt() <= 0)
                                    error("Array size should be greater than zero")
                                else
                                    null
                            }
                        }

                        removeWhen { settings.testingParameter.booleanBinding { it is ArraySize } }
                    }

                    field(ClientsCount.toString()) {
                        textfield(settings.clientsCount) {
                            stripNonNumeric("")
                            textFormatter = TextFormatter(
                                NumberStringConverter("########"),
                                settings.clientsCount.value
                            )

                            validator {
                                if (it.isNullOrBlank())
                                    return@validator error("This field is required")

                                if (it.toInt() <= 0)
                                    error("Amount of clients should be greater than zero")
                                else
                                    null
                            }
                        }

                        removeWhen { settings.testingParameter.booleanBinding { it is ClientsCount } }
                    }

                    field(ClientDelay.toString()) {
                        textfield(settings.clientDelay) {
                            stripNonNumeric("")
                            textFormatter = TextFormatter(
                                NumberStringConverter("########"),
                                settings.clientDelay.value
                            )

                            validator {
                                if (it.isNullOrBlank())
                                    return@validator error("This field is required")

                                if (it.toInt() < 0)
                                    error("Client delay should not be negative")
                                else
                                    null
                            }
                        }

                        removeWhen { settings.testingParameter.booleanBinding { it is ClientDelay } }
                    }
                }

                fieldset("Other settings") {
                    field("Server port") {
                        textfield(settings.serverPort) {
                            stripNonNumeric("")
                            textFormatter = TextFormatter(
                                NumberStringConverter("########"),
                                settings.serverPort.value
                            )

                            validator {
                                if (it.isNullOrBlank())
                                    return@validator error("This field is required")

                                if (it.toInt() !in 0..65535)
                                    error("Server port should be in [0..65535] range, inclusive")
                                else
                                    null
                            }
                        }
                    }

                    field("Server thread pool size") {
                        textfield(settings.serverThreadPoolSize) {
                            stripNonNumeric("")
                            textFormatter = TextFormatter(
                                NumberStringConverter("########"),
                                settings.serverThreadPoolSize.value
                            )

                            validator {
                                if (it.isNullOrBlank())
                                    return@validator error("This field is required")

                                if (it.toInt() <= 0)
                                    error("Thread pool size should be greater than zero")
                                else
                                    null
                            }
                        }
                    }
                }

                buttonbar {
                    button("Run!") {
                        setOnAction {
                            settings.commit {
                                find<ExecutionView>().openModal(block = true)
                            }
                        }

                        shortcut(KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHORTCUT_DOWN))
                    }
                }
            }
        }
    }
}

class ArchitectureComparatorApp : App(MainView::class) {
    override fun start(stage: Stage) {
        with(stage) {
            width = WINDOW_WIDTH
            height = WINDOW_HEIGHT
            isResizable = false

            super.start(this)
        }
    }
}

fun main(args: Array<String>): Unit = launch<ArchitectureComparatorApp>(args)
