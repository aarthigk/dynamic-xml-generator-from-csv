package com.testarchive.dynamicxmlgenerator;
/**
 * The {@code DynamicXMLGeneratorFromCSV} class is a JavaFX application that allows you to generate XML
 * files based on data from a CSV file.
 *
 * <p>It provides a graphical user interface for selecting a CSV file, specifying the XML structure,
 * and generating an XML file from the CSV data.
 *
 * <p>This class uses libraries like OpenCSV and Java's DOM API to handle CSV parsing and XML generation.
 *
 * <p>Usage:
 * 1. Launch the application.
 * 2. Enter the XML structure details, including root element, child element (optional),
 *    sibling element (optional), and the number of columns.
 * 3. Browse and select a CSV file.
 * 4. Click the "Generate XML" button to create an XML file based on the CSV data.
 *
 * <p>Dependencies:
 * - JavaFX (Java's platform for creating rich client applications)
 * - OpenCSV (a library for reading and writing CSV files)
 *
 * <p>Author: Aarthi
 * Version: 1.0
 * Date: September 29, 2023
 */

import javafx.application.Application;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.*;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class DynamicXMLGeneratorFromCSV extends Application {
    private File selectedFile;
    private Label filePathLabel;
    private TextField rootElementField;
    private TextField childElementField;
    private TextField siblingElementField;
    private TextField numColumnsField;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Dynamic XML Generator");

        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Dynamic XML Generator");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        HBox rootElementBox = new HBox(10);
        rootElementBox.setAlignment(Pos.CENTER);
        Label rootElementLabel = new Label("Root Element:");
        rootElementField = new TextField();
        rootElementBox.getChildren().addAll(rootElementLabel, rootElementField);

        HBox childElementBox = new HBox(10);
        childElementBox.setAlignment(Pos.CENTER);
        Label childElementLabel = new Label("Child Element (Optional):");
        childElementField = new TextField();
        childElementBox.getChildren().addAll(childElementLabel, childElementField);

        HBox siblingElementBox = new HBox(10);
        siblingElementBox.setAlignment(Pos.CENTER);
        Label siblingElementLabel = new Label("Sibling Element (Optional):");
        siblingElementField = new TextField();
        siblingElementBox.getChildren().addAll(siblingElementLabel, siblingElementField);

        HBox numColumnsBox = new HBox(10);
        numColumnsBox.setAlignment(Pos.CENTER);
        Label numColumnsLabel = new Label("Number of Columns:");
        numColumnsField = new TextField();
        numColumnsBox.getChildren().addAll(numColumnsLabel, numColumnsField);

        Button browseButton = new Button("Browse CSV File");
        filePathLabel = new Label("Selected File: None");

        Button generateButton = new Button("Generate XML");

        browseButton.setOnAction(e -> browseCSVFile(primaryStage));
        generateButton.setOnAction(e -> generateXML());

        vbox.getChildren().addAll(
                titleLabel, rootElementBox, childElementBox, siblingElementBox, numColumnsBox,
                browseButton, filePathLabel, generateButton
        );

        Scene scene = new Scene(vbox, 400, 300);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    private void browseCSVFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            filePathLabel.setText("Selected File: " + selectedFile.getName());
        } else {
            filePathLabel.setText("Selected File: None");
        }
    }

    private void generateXML() {
        if (selectedFile == null) {
            showErrorDialog("Please select a CSV file.");
            return;
        }

        String rootElementName = rootElementField.getText().trim();
        String childElementName = childElementField.getText().trim();
        String siblingElementName = siblingElementField.getText().trim();
        String numColumnsStr = numColumnsField.getText().trim();

        if (rootElementName.isEmpty() || numColumnsStr.isEmpty()) {
            showErrorDialog("Please enter a root element name and the number of columns.");
            return;
        }

        int numColumns;
        try {
            numColumns = Integer.parseInt(numColumnsStr);
        } catch (NumberFormatException e) {
            showErrorDialog("Invalid number of columns. Please enter a valid integer.");
            return;
        }

        if (numColumns <= 0) {
            showErrorDialog("Number of columns must be greater than zero.");
            return;
        }

        try {
            // Initialize XML document
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            // Create root element
            Element rootElement = doc.createElement(rootElementName);
            doc.appendChild(rootElement);

            // Initialize OpenCSV reader
            CSVReader csvReader = new CSVReaderBuilder(new FileReader(selectedFile)).withSkipLines(1).build();
            String[] nextRecord;

            // Read CSV and create XML elements
            while ((nextRecord = csvReader.readNext()) != null) {
                List<String> rowData = Arrays.asList(nextRecord);
                if (rowData.size() >= numColumns) {
                    Element row = doc.createElement(childElementName.isEmpty() ? "ROW" : childElementName);
                    rootElement.appendChild(row);

                    for (int i = 0; i < numColumns; i++) {
                        Element element = doc.createElement(siblingElementName.isEmpty() ? "Column" + (i + 1) : siblingElementName);
                        element.appendChild(doc.createTextNode(rowData.get(i)));
                        row.appendChild(element);
                    }
                }
            }

            // Write the content into an XML file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            File outputFile = new File(selectedFile.getParentFile(), "output.xml");
            StreamResult result = new StreamResult(outputFile);
            transformer.transform(source, result);

            showAlert("XML file generated successfully!\nOutput file path: " + outputFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("An error occurred during generation.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

  
}
