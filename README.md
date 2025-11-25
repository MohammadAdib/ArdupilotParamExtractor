# Ardupilot Parameter Extractor

A JavaFX application built with Kotlin for filtering and extracting Ardupilot parameters by category.

<img width="1827" height="1039" alt="image" src="https://github.com/user-attachments/assets/93ea8982-a59d-490a-bfdd-55c897e74a50" />


## Features

- **Three-Pane Layout**:
  - **Left Pane**: Displays all parameters from the loaded file with color-coded rows with individual checkboxes based on parameter category
  - **Middle Pane**: Category selection checkboxes to filter parameters
  - **Right Pane**: Displays filtered parameters based on selected categories or sections of categories


- **Parameter Categories**:
  - QuadPlane / VTOL - Purple
  - PID Controllers (Attitude) - Crimson
  - PID Controllers (Position) - Orange Red
  - TECS (Energy Control) - Teal
  - Servos & Outputs - Forest Green
  - On-Screen Display - Gold
  - Failsafe & Safety - Firebrick
  - Serial Ports - Steel Blue
  - GPS & Navigation - Deep Sky Blue
  - Battery & Power - Dark Orange
  - Sensors - Dark Violet
  - Camera & Gimbal - Deep Pink
  - Mission & Auto - Dodger Blue
  - Logging & Telemetry - Gray
  - Relays & Switches - Saddle Brown
  - System & Scheduler - Dim Gray
  - Terrain Following - Yellow Green
  - Airspeed Control - Cornflower Blue
  - Misc Parameters - Dark Gray

## Usage

1. **Open Parameter File**: Click the "Open file" button and select an Ardupilot parameter file (`.param`, `.parm`, or `.txt`)
2. **View Parameters**: All parameters will be displayed in the left pane with color-coding based on their category
3. **Filter Parameters**: Use the checkboxes in the middle pane to select which categories to include in the filtered output, or select individual parameters to include in the left pane
4. **Export Filtered Parameters**: Click "Export" to save the filtered parameters to a new file

## Parameter File Format

The application supports Ardupilot parameter files in the following formats:

```
PARAM_NAME,VALUE
```

or

```
PARAM_NAME VALUE
```

Comments (lines starting with `#`) and empty lines are ignored.

## Sample File

A sample parameter file (`sample_parameters.param`) is included in the repository for testing purposes.

## IntelliJ IDEA Setup

1. Open the project in IntelliJ IDEA
2. Maven should auto-import dependencies
3. Run configuration:
   - Main class: `com.ardupilot.paramextractor.MainApp`
   - VM options: `--module-path <path-to-javafx-sdk>/lib --add-modules javafx.controls,javafx.fxml`

Alternatively, use the Maven run configuration with goal: `javafx:run`

## Build Installer
Build JAR, and then run the below command from \out\artifacts\parameter_extractor_jar
```
 jpackage --type msi --app-version "1.0.0" --input . --dest . --main-jar parameter-extractor.jar --main-class com.titanuas.paramextractor.MainApp --module-path "C:\Program Files\Java\javafx-jmods-26" --add-modules javafx.controls,javafx.fxml,javafx.swing,javafx.base,javafx.graphics --win-shortcut --win-menu --icon "icon.ico" --name "Parameter Extractor"
```