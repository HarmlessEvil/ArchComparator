package ru.itmo.chori.archcomparator.gui

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.stringBinding

internal class RunSettings {
    val architectures: ObservableList<Architecture> = FXCollections.observableArrayList(
        ManyThreadedArchitecture, NonBlockingArchitecture, AsynchronousArchitecture
    )
    val selectedArchitecture = SimpleObjectProperty(architectures[0])
    val selectedArchitectureDescription = selectedArchitecture.stringBinding { it?.description }

    // TODO: Validate { it > 0 }
    val queriesPerClient = SimpleIntegerProperty(100)

    val testingParameters: ObservableList<TestingParameter> = FXCollections.observableArrayList(
        ArraySize, ClientsCount, ClientDelay
    )
    val selectedTestingParameter = SimpleObjectProperty(testingParameters[0])
    val selectedTestingParameterDescription = selectedTestingParameter.stringBinding { it?.description }

    val minParameterValue = SimpleIntegerProperty() // TODO: Validate *required*; Validate min <= max
    val maxParameterValue = SimpleIntegerProperty() // TODO: Validate *required*; Validate max >= min
    val parameterStep = SimpleIntegerProperty() // TODO: Validate *required*; Validate step > 0

    val serverPort = SimpleIntegerProperty(8080) // TODO: Validate { it in 0..65535 }
    val serverThreadPoolSize = SimpleIntegerProperty(4) // TODO: Validate { it > 0 }
}
