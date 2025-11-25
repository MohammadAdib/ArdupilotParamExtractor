package com.titanuas.paramextractor.ui

import com.titanuas.paramextractor.model.Parameter
import com.titanuas.paramextractor.model.ParameterCategory
import com.titanuas.paramextractor.service.ParameterParser
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.scene.shape.Rectangle
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.util.Duration
import java.io.File
import java.net.URL
import java.util.*
import java.util.prefs.Preferences
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class MainViewController : Initializable {
    @FXML private lateinit var openFileButton: Button
    @FXML private lateinit var searchField: TextField
    @FXML private lateinit var exportButton: Button
    @FXML private lateinit var leftPaneLabel: Label
    @FXML private lateinit var leftListView: ListView<Parameter>
    @FXML private lateinit var categoryPane: VBox
    @FXML private lateinit var rightPaneLabel: Label
    @FXML private lateinit var rightListView: ListView<Parameter>

    private val parameterParser = ParameterParser()
    private var allParameters = listOf<Parameter>()
    private val prefs = Preferences.userNodeForPackage(MainViewController::class.java)
    private var searchText = ""
    private var searchDebounceTask: CompletableFuture<Void>? = null
    private var updateDebounceTask: CompletableFuture<Void>? = null
    private val categoryCheckBoxes = mutableMapOf<ParameterCategory, CheckBox>()
    private val parameterRows = mutableListOf<ParameterRow>()
    private val selectedParameters = mutableSetOf<Parameter>()
    private var isUpdating = false
    private var lastOpenedFileName: String = "parameters.param"

    var stage: Stage? = null

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        setupListViews()
        setupSearchListener()
        updateExportButtonState()
    }

    private fun setupListViews() {
        // Completely disable selection for both ListViews
        leftListView.isFocusTraversable = false
        rightListView.isFocusTraversable = false

        // Disable selection model
        leftListView.selectionModel = null
        rightListView.selectionModel = null

        // Set cell factory for left list (with checkboxes)
        leftListView.setCellFactory {
            ParameterCell(
                { param, selected -> onParameterSelectionChanged(param, selected) },
                showCheckbox = true,
                selectedParameters = selectedParameters
            )
        }

        // Set cell factory for right list (without checkboxes)
        rightListView.setCellFactory {
            ParameterCell(
                { _, _ -> },
                showCheckbox = false,
                selectedParameters = selectedParameters
            )
        }
    }

    private fun setupSearchListener() {
        searchField.textProperty().addListener { _, _, newValue ->
            debounceSearch(newValue)
        }
    }

    private fun debounceSearch(newValue: String) {
        // Cancel existing task
        searchDebounceTask?.cancel(true)

        // Create new debounced task
        searchDebounceTask = CompletableFuture.runAsync({
            Thread.sleep(250)
        }).thenRun {
            javafx.application.Platform.runLater {
                searchText = newValue.lowercase()
                updateAllDisplays()
            }
        }
    }

    @FXML
    private fun onOpenFile() {
        val fileChooser = FileChooser().apply {
            title = "Open Parameter File"
            extensionFilters.addAll(
                FileChooser.ExtensionFilter("Parameter Files", "*.param", "*.parm", "*.txt"),
                FileChooser.ExtensionFilter("All Files", "*.*")
            )

            val lastDir = prefs.get("lastDirectory", null)
            if (lastDir != null) {
                val dir = File(lastDir)
                if (dir.exists() && dir.isDirectory) {
                    initialDirectory = dir
                }
            }
        }

        val file = fileChooser.showOpenDialog(stage)
        if (file != null) {
            prefs.put("lastDirectory", file.parent)
            lastOpenedFileName = file.name
            openFileButton.isMnemonicParsing = false
            openFileButton.text = file.nameWithoutExtension
            loadParameters(file)
        }
    }

    @FXML
    private fun onSelectAll() {
        isUpdating = true
        categoryCheckBoxes.values.forEach {
            it.isSelected = true
            it.isIndeterminate = false
        }
        selectedParameters.addAll(allParameters)
        leftListView.refresh()
        isUpdating = false
        scheduleFilterUpdate()
    }

    @FXML
    private fun onDeselectAll() {
        isUpdating = true
        categoryCheckBoxes.values.forEach {
            it.isSelected = false
            it.isIndeterminate = false
        }
        selectedParameters.clear()
        leftListView.refresh()
        isUpdating = false
        scheduleFilterUpdate()
    }

    @FXML
    private fun onExport() {
        if (selectedParameters.isEmpty()) {
            showError("No Data", "There are no selected parameters to export.")
            return
        }

        // Generate filtered file name from original
        val baseName = lastOpenedFileName.substringBeforeLast(".")
        val extension = lastOpenedFileName.substringAfterLast(".", "param")
        val filteredFileName = "${baseName}_filtered.${extension}"

        val fileChooser = FileChooser().apply {
            title = "Export Filtered Parameters"
            initialFileName = filteredFileName
            extensionFilters.addAll(
                FileChooser.ExtensionFilter("Parameter Files", "*.param"),
                FileChooser.ExtensionFilter("Text Files", "*.txt"),
                FileChooser.ExtensionFilter("All Files", "*.*")
            )

            val lastDir = prefs.get("lastDirectory", null)
            if (lastDir != null) {
                val dir = File(lastDir)
                if (dir.exists() && dir.isDirectory) {
                    initialDirectory = dir
                }
            }
        }

        val file = fileChooser.showSaveDialog(stage)
        if (file != null) {
            try {
                prefs.put("lastDirectory", file.parent)

                val filteredBySearch = if (searchText.isEmpty()) {
                    selectedParameters.toList()
                } else {
                    selectedParameters.filter {
                        it.name.lowercase().contains(searchText) ||
                        it.value.lowercase().contains(searchText)
                    }
                }

                val content = parameterParser.exportParameters(filteredBySearch.sortedBy { it.name })
                file.writeText(content)
            } catch (e: Exception) {
                showError("Export Failed", "Failed to export parameters: ${e.message}")
            }
        }
    }

    private fun loadParameters(file: File) {
        try {
            allParameters = parameterParser.parseParameterFile(file)
            selectedParameters.clear()
            updateCategoryCheckboxes()
            updateAllDisplays()
            // Scroll to top on left pane after loading new file
            leftListView.scrollTo(0)
        } catch (e: Exception) {
            showError("Error loading file", "Failed to load parameter file: ${e.message}")
        }
    }

    private fun updateAllDisplays() {
        // Filter all parameters by search text
        val filteredBySearch = if (searchText.isEmpty()) {
            allParameters
        } else {
            allParameters.filter {
                it.name.lowercase().contains(searchText)
            }
        }

        // Left pane shows filtered parameters
        displayParameters(leftListView, filteredBySearch)
        // Right pane shows selected parameters filtered by search
        updateFilteredParameters()
        updateExportButtonState()
    }

    private fun onParameterSelectionChanged(parameter: Parameter, selected: Boolean) {
        if (isUpdating) return

        if (selected) {
            selectedParameters.add(parameter)
        } else {
            selectedParameters.remove(parameter)
        }
        updateCategoryCheckboxState(parameter.category)
        scheduleFilterUpdate()
    }

    private fun updateCategoryCheckboxState(category: ParameterCategory) {
        if (isUpdating) return

        val categoryCheckBox = categoryCheckBoxes[category] ?: return
        val categoryParams = allParameters.filter { it.category == category }
        val selectedCount = categoryParams.count { it in selectedParameters }

        isUpdating = true
        when {
            selectedCount == 0 -> {
                categoryCheckBox.isIndeterminate = false
                categoryCheckBox.isSelected = false
            }
            selectedCount == categoryParams.size -> {
                categoryCheckBox.isIndeterminate = false
                categoryCheckBox.isSelected = true
            }
            else -> {
                categoryCheckBox.isIndeterminate = true
            }
        }
        isUpdating = false
    }

    private fun scheduleFilterUpdate() {
        updateDebounceTask?.cancel(true)
        updateDebounceTask = CompletableFuture.runAsync({
            Thread.sleep(50)
        }).thenRun {
            javafx.application.Platform.runLater {
                updateFilteredParameters()
                updateExportButtonState()
            }
        }
    }

    private fun updateExportButtonState() {
        exportButton.isDisable = selectedParameters.isEmpty()
    }

    private fun updateCategoryCheckboxes() {
        val existingCategories = allParameters.map { it.category }.toSet()

        categoryPane.children.clear()
        categoryCheckBoxes.clear()

        ParameterCategory.values()
            .filter { it != ParameterCategory.OTHER && it in existingCategories }
            .forEach { category ->
                val checkBox = CheckBox(category.displayName).apply {
                    isSelected = false
                    isIndeterminate = false
                    isAllowIndeterminate = true
                    setOnAction { onCategoryCheckboxChanged(category, this) }

                    val indicator = Rectangle(12.0, 12.0, category.color)
                    graphic = indicator
                }
                categoryCheckBoxes[category] = checkBox
                categoryPane.children.add(checkBox)
            }

        if (ParameterCategory.OTHER in existingCategories) {
            val otherCheckBox = CheckBox(ParameterCategory.OTHER.displayName).apply {
                isSelected = false
                isIndeterminate = false
                isAllowIndeterminate = true
                setOnAction { onCategoryCheckboxChanged(ParameterCategory.OTHER, this) }

                val indicator = Rectangle(12.0, 12.0, ParameterCategory.OTHER.color)
                graphic = indicator
            }
            categoryCheckBoxes[ParameterCategory.OTHER] = otherCheckBox
            categoryPane.children.add(otherCheckBox)
        }
    }

    private fun onCategoryCheckboxChanged(category: ParameterCategory, checkBox: CheckBox) {
        if (isUpdating) return

        isUpdating = true

        if (checkBox.isIndeterminate) {
            checkBox.isIndeterminate = false
            checkBox.isSelected = true
        }

        val shouldSelect = checkBox.isSelected
        val categoryParams = allParameters.filter { it.category == category }

        categoryParams.forEach { param ->
            if (shouldSelect) {
                selectedParameters.add(param)
            } else {
                selectedParameters.remove(param)
            }
        }

        // Refresh ListView to update checkboxes
        leftListView.refresh()

        isUpdating = false
        scheduleFilterUpdate()
    }

    private fun displayParameters(listView: ListView<Parameter>, parameters: List<Parameter>) {
        parameterRows.clear()
        listView.items.setAll(parameters)

        // Update left pane count
        leftPaneLabel.text = "Original Parameters (${leftListView.items.size})"

        // Force refresh to update checkbox states
        listView.refresh()
    }

    private fun updateFilteredParameters() {
        val filteredBySearch = if (searchText.isEmpty()) {
            selectedParameters.toList()
        } else {
            selectedParameters.filter {
                it.name.lowercase().contains(searchText)
            }
        }

        val sorted = filteredBySearch.sortedBy { it.name }
        rightListView.items.setAll(sorted)

        // Update right pane count
        rightPaneLabel.text = "Filtered Parameters (${rightListView.items.size})"
    }

    private fun showError(title: String, message: String) {
        Alert(Alert.AlertType.ERROR).apply {
            this.title = title
            headerText = null
            contentText = message
            showAndWait()
        }
    }

    private fun showInfo(title: String, message: String) {
        Alert(Alert.AlertType.INFORMATION).apply {
            this.title = title
            headerText = null
            contentText = message
            showAndWait()
        }
    }
}
