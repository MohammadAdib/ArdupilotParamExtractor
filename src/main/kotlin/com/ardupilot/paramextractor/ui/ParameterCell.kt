package com.ardupilot.paramextractor.ui

import com.ardupilot.paramextractor.model.Parameter
import javafx.scene.control.ListCell

class ParameterCell(
    private val onSelectionChanged: (Parameter, Boolean) -> Unit,
    private val showCheckbox: Boolean = true,
    private val selectedParameters: Set<Parameter>
) : ListCell<Parameter>() {

    override fun updateItem(item: Parameter?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            text = null
        } else {
            // Always create new ParameterRow for correct data
            val row = ParameterRow(item, onSelectionChanged, showCheckbox)

            // Update checkbox state based on selectedParameters
            if (showCheckbox) {
                row.checkBox.isSelected = item in selectedParameters
            }

            graphic = row
            text = null
        }
    }
}
