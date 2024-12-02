module com.example.ab005 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;


    requires java.sql;

    opens com.example.ab005 to javafx.fxml;
    exports com.example.ab005;
}