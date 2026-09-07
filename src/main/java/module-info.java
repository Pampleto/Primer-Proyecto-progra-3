module com.example.primerproyectoprogra3 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.primerproyectoprogra3 to javafx.fxml;
    exports com.example.primerproyectoprogra3;
}