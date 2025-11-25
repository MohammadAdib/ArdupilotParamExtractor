module com.titanuas {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    requires kotlin.stdlib;
    requires java.prefs;
    requires jfxutils;

    opens com.titanuas.paramextractor to javafx.fxml, com.google.gson;
    opens com.titanuas.paramextractor.ui to javafx.fxml, com.google.gson;
    exports com.titanuas.paramextractor to javafx.graphics, javafx.base, javafx.controls;
}
