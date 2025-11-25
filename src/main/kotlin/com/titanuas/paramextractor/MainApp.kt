package com.titanuas.paramextractor

import com.titanuas.paramextractor.ui.MainViewController
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.scene.image.Image

class MainApp : Application() {

    override fun start(primaryStage: Stage) {
        primaryStage.title = "Ardupilot Parameter Extractor - By Titan Dynamics"

        // Load FXML
        val fxmlLoader = FXMLLoader(javaClass.getResource("/com/titanuas/paramextractor/MainView.fxml"))
        val root = fxmlLoader.load<Parent>()

        // Get controller and set stage reference
        val controller = fxmlLoader.getController<MainViewController>()
        controller.stage = primaryStage

        val scene = Scene(root, 1460.0, 800.0)

        // Load dark theme CSS
        val cssResource = javaClass.getResource("/com/titanuas/paramextractor/dark.css")
        if (cssResource != null) {
            scene.stylesheets.add(cssResource.toExternalForm())
        }

        // Set window icon if available
        val iconResource = javaClass.getResource("/com/titanuas/paramextractor/icon.png")
        if (iconResource != null) {
            try {
                primaryStage.icons.add(Image(iconResource.toExternalForm()))
            } catch (_: Exception) {
                // ignore if icon cannot be loaded
            }
        }

        primaryStage.scene = scene
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(MainApp::class.java, *args)
        }
    }
}
