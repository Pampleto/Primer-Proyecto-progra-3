package com.example.primerproyectoprogra3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Clase principal de la aplicacion TravelSafe.
 *
 * Carga la ventana principal del sistema junto con sus estilos
 * CSS y la muestra en pantalla.
 */
public class HelloApplication extends Application {

    /**
     * Punto de entrada grafico de la aplicacion.
     *
     * Recibe el escenario principal de JavaFX, carga el archivo FXML
     * de la ventana principal y los estilos CSS, y muestra la ventana.
     *
     * @param stage escenario principal proporcionado por JavaFX
     * @throws IOException si no se puede cargar el archivo FXML
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/vista/principal.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 640);
        scene.getStylesheets().add(Objects.requireNonNull(
                HelloApplication.class.getResource("/css/estilos.css")).toExternalForm());
        stage.setTitle("TravelSafe - Cotizador de Seguros de Viaje");
        stage.setScene(scene);
        stage.show();
    }
}
