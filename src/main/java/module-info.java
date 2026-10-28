module com.example.primerproyectoprogra3 {

    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // Base de datos MySQL
    requires java.sql;
    requires mysql.connector.j;

    // Paquetes de la aplicacion
    opens com.example.primerproyectoprogra3 to javafx.fxml;
    opens controlador to javafx.fxml;

    exports com.example.primerproyectoprogra3;
}
