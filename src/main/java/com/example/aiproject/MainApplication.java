package com.example.aiproject;

import com.example.aiproject.Classes.DatasetReader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // To let the user pick their Excel dataset before the application loads
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Open Dataset (.xlsx)");
        chooser.getExtensionFilters().add(
            new javafx.stage.FileChooser.ExtensionFilter("Excel files", "*.xlsx"));
        java.io.File file = chooser.showOpenDialog(stage);
        if (file == null) { System.exit(0); return; }

        try {
            Launcher.data = DatasetReader.readDataset(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            // To inform the user when the dataset cannot be parsed instead of silently crashing
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR,
                "Failed to read dataset:\n" + e.getMessage());
            alert.showAndWait();
            System.exit(1);
        }

        // To load the main FXML layout and apply the dark stylesheet
        FXMLLoader loader = new FXMLLoader(
            MainApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 800);
        scene.getStylesheets().add(
            MainApplication.class.getResource("styles.css").toExternalForm());
        stage.setTitle("GA Exam Scheduler — COMP338");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
}
