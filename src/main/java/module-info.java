module com.example.aiproject {
    requires org.apache.poi.ooxml;

    opens com.example.aiproject to javafx.fxml;
    exports com.example.aiproject;
}