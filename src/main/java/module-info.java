module com.example.dynamicxmlgenerator {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;
    requires com.opencsv;


    opens com.testarchive.dynamicxmlgenerator to javafx.fxml;
    exports com.testarchive.dynamicxmlgenerator;
}