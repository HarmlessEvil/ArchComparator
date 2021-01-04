package ru.itmo.chori.archcomparator.gui

import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import ru.itmo.chori.archcomparator.client.runClientAndMeasureTime
import tornadofx.*
import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.RejectedExecutionException
import kotlin.concurrent.thread
import kotlin.math.ceil

class ExecutionView : Fragment("Architecture Comparator – Executing...") {
    private val settings: RunSettingsModel by inject()

    private val progress = SimpleDoubleProperty(0.0)

    private val parameterRangeSize = (settings.maxParameterValue - settings.minParameterValue).doubleValue()
    private val totalSteps = ceil(parameterRangeSize / settings.parameterStep.doubleValue() + 1).toInt()

    @Volatile
    private var isClosed = false

    private val runner = thread {
        val clientsThreadPool = Executors.newCachedThreadPool { runnable ->
            thread(start = false, name = "client") { runnable.run() }
        }

        try {
            for (i in 0 until totalSteps) {
                if (Thread.interrupted()) {
                    return@thread
                }

                val server = serverFactory(
                    settings.architecture.value,
                    settings.serverPort.value,
                    settings.serverThreadPoolSize.value
                )

                try {
                    server.use {
                        val parameterValue =
                            (settings.minParameterValue.value + progress.value * parameterRangeSize).toInt()

                        val arraySize = parameterValue.takeIf { settings.testingParameter.value is ArraySize }
                            ?: settings.arraySize.value
                        val delay = (parameterValue.takeIf { settings.testingParameter.value is ClientDelay }
                            ?: settings.clientDelay.value).toLong()
                        val clientsCount = parameterValue.takeIf { settings.testingParameter.value is ClientsCount }
                            ?: settings.clientsCount.value

                        val clientMeasuredTimeFutures: List<Future<Duration>> = List<Future<Duration>>(clientsCount) {
                            clientsThreadPool.submit(Callable {
                                runClientAndMeasureTime(
                                    serverPort = settings.serverPort.value,
                                    dataSize = arraySize,
                                    delayBeforeNextMessage = Duration.ofMillis(delay),
                                    messageCount = settings.queriesPerClient.value
                                )
                            })
                        }

                        val clientMeasuredTimes = clientMeasuredTimeFutures.map { it.get() }
                        println(clientMeasuredTimes)
                    }
                } catch (e: RejectedExecutionException) {
                    if (!isClosed) {
                        throw e
                    }
                }

                println(server.tasksTime)
                runLater { progress.set((i + 1).toDouble() / totalSteps) }
            }
        } catch (e: InterruptedException) {
            return@thread
        } catch (e: Exception) {
            runLater { close() }
            throw e
        } finally {
            clientsThreadPool.shutdown()
        }
    }

    override val root = gridpane {
        row {
            vbox {
                alignment = Pos.CENTER

                text("Selected architecture is ${settings.architecture.value}")
                text(
                    "Testing ${settings.testingParameter.value} in range " +
                            "[${settings.minParameterValue.value}..${settings.maxParameterValue.value}] " +
                            "with step ${settings.parameterStep.value}"
                )

                gridpaneConstraints {
                    margin = Insets(20.0, 20.0, 25.0, 20.0)
                }
            }
        }

        row {
            hbox(20) {
                alignment = Pos.CENTER

                label(progress.stringBinding {
                    "Completed ${(it!!.toDouble() * totalSteps).toInt()} runs of $totalSteps total"
                })
                progressindicator(progress)

                gridpaneConstraints {
                    margin = Insets(0.0, 20.0, 20.0, 20.0)
                }
            }
        }
    }

    override fun onUndock() {
        isClosed = true

        runner.interrupt()
        runner.join() // Do I need to do it?
    }
}
