package ru.itmo.chori.archcomparator.gui

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.ItemViewModel

class RunSettings {
    companion object {
        val architectures: ObservableList<Architecture> = FXCollections.observableArrayList(
            ManyThreadedArchitecture, NonBlockingArchitecture, AsynchronousArchitecture
        )

        val testingParameters: ObservableList<TestingParameter> = FXCollections.observableArrayList(
            ArraySize, ClientsCount, ClientDelay
        )
    }

    val selectedArchitecture = SimpleObjectProperty(this, "architecture", architectures[0])

    val queriesPerClient = SimpleIntegerProperty(this, "queries per client", 100)

    val selectedTestingParameter = SimpleObjectProperty(this, "testing parameter", testingParameters[0])

    val minParameterValue = SimpleIntegerProperty()
    val maxParameterValue = SimpleIntegerProperty()
    val parameterStep = SimpleIntegerProperty()

    val serverPort = SimpleIntegerProperty(this, "server port", 8080)
    val serverThreadPoolSize = SimpleIntegerProperty(this, "server thread pool size", 4)
}

class RunSettingsModel(settings: RunSettings? = RunSettings()) : ItemViewModel<RunSettings>(settings) {
    val architecture = bind(RunSettings::selectedArchitecture)
    val queriesPerClient = bind(RunSettings::queriesPerClient)
    val testingParameter = bind(RunSettings::selectedTestingParameter)
    val minParameterValue = bind(RunSettings::minParameterValue)
    val maxParameterValue = bind(RunSettings::maxParameterValue)
    val parameterStep = bind(RunSettings::parameterStep)
    val serverPort = bind(RunSettings::serverPort)
    val serverThreadPoolSize = bind(RunSettings::serverThreadPoolSize)

    override fun toString(): String {
        return "RunSettingsModel(" +
                "architecture=${architecture.get()}, " +
                "queriesPerClient=${queriesPerClient.get()}, " +
                "testingParameter=${testingParameter.get()}, " +
                "minParameterValue=${minParameterValue.get()}, " +
                "maxParameterValue=${maxParameterValue.get()}, " +
                "parameterStep=${parameterStep.get()}, " +
                "serverPort=${serverPort.get()}, " +
                "serverThreadPoolSize=${serverThreadPoolSize.get()}" +
                ")"
    }
}
