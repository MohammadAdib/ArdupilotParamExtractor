package com.ardupilot.paramextractor.ui

import com.ardupilot.paramextractor.model.Parameter
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.geometry.Side
import javafx.scene.control.CheckBox
import javafx.scene.control.Button
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.Tooltip
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import java.awt.Desktop
import java.net.URI

class ParameterRow(
    val parameter: Parameter,
    val onSelectionChanged: (Parameter, Boolean) -> Unit,
    val showCheckbox: Boolean = true
) : HBox(8.0) {
    val checkBox: CheckBox = CheckBox()

    init {
        padding = Insets(4.0, 8.0, 4.0, 8.0)
        alignment = Pos.CENTER_LEFT

        // Checkbox for selection (fixed width) - only if showCheckbox is true
        if (showCheckbox) {
            checkBox.apply {
                isSelected = true
                minWidth = 25.0
                maxWidth = 25.0
                setOnAction {
                    onSelectionChanged(parameter, isSelected)
                }
            }
            children.add(checkBox)
        }

        // Parameter name with colored text
        val nameLabel = Label(parameter.name).apply {
            style = "-fx-font-weight: bold; -fx-text-fill: ${toRgbString(parameter.category.color)};"
        }

        // Dotted line separator (grows to fill space)
        val separator = Region().apply {
            style = "-fx-border-style: dotted none none none; -fx-border-color: #555555; -fx-border-width: 0 0 1 0; -fx-translate-y: -3;"
            minHeight = 1.0
            maxHeight = 1.0
            HBox.setHgrow(this, Priority.ALWAYS)
        }

        // Parameter value (right-aligned)
        val valueLabel = Label(parameter.value).apply {
            style = "-fx-text-fill: lightgray;"
        }

        // Info icon/button to open documentation
        val infoButton = Button("i").apply {
            styleClass.add("info-button")
            tooltip = Tooltip("Show docs for ${parameter.name}")

            setOnAction {
                val menu = ContextMenu().apply {
                    val planeUrl = docsUrl("https://ardupilot.org/plane/docs/parameters.html", parameter.name)
                    val copterUrl = docsUrl("https://ardupilot.org/copter/docs/parameters.html", parameter.name)
                    val roverUrl = docsUrl("https://ardupilot.org/rover/docs/parameters.html", parameter.name)
                    val subUrl = docsUrl("https://ardupilot.org/sub/docs/parameters.html", parameter.name)
                    val trackerUrl = docsUrl("https://ardupilot.org/antennatracker/docs/parameters.html", parameter.name)

                    items.addAll(
                        MenuItem("Open Plane docs").apply { setOnAction { openInBrowser(planeUrl) } },
                        MenuItem("Open Copter docs").apply { setOnAction { openInBrowser(copterUrl) } },
                        MenuItem("Open Rover docs").apply { setOnAction { openInBrowser(roverUrl) } },
                        MenuItem("Open Sub docs").apply { setOnAction { openInBrowser(subUrl) } },
                        MenuItem("Open AntennaTracker docs").apply { setOnAction { openInBrowser(trackerUrl) } }
                    )
                }
                menu.show(this, Side.BOTTOM, 0.0, 0.0)
            }
        }

        children.addAll(nameLabel, separator, valueLabel, infoButton)
    }

    private fun toRgbString(color: Color): String {
        val r = (color.red * 255).toInt()
        val g = (color.green * 255).toInt()
        val b = (color.blue * 255).toInt()
        return "rgb($r, $g, $b)"
    }

    private fun docsUrl(base: String, paramName: String): String {
        val anchor = paramName.lowercase().replace("_", "-")
        return "$base#$anchor"
    }

    private fun openInBrowser(url: String) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI(url))
            }
        } catch (_: Exception) {
            // Silently ignore if unable to open browser
        }
    }
}
