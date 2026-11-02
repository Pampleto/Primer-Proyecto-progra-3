package red;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.net.*;
import java.time.Duration;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(10)
class RedCotizacionesTest {
    /** Conserva incluso propiedades VM que existieran antes de la prueba. */
    private void conConfiguracion(String escucha, String destino, Runnable prueba) {
        String anteriorEscucha = System.getProperty("travelsafe.sockets.escucha");
        String anteriorDestino = System.getProperty("travelsafe.sockets.destino");
        try {
            System.setProperty("travelsafe.sockets.escucha", escucha);
            System.setProperty("travelsafe.sockets.destino", destino);
            prueba.run();
        } finally {
            restaurar("travelsafe.sockets.escucha", anteriorEscucha);
            restaurar("travelsafe.sockets.destino", anteriorDestino);
        }
    }

    private void restaurar(String clave, String valor) {
        if (valor == null) System.clearProperty(clave);
        else System.setProperty(clave, valor);
    }

    @Test void envioAsincronoRealYCierre() throws Exception {
        var recibido = new CountDownLatch(1);
        try (var servidor = new ServidorCotizaciones(0, id -> {
            if (id == 77) recibido.countDown();
        })) {
            conConfiguracion("0", "" + servidor.puerto(), () -> {
                try (var red = RedCotizaciones.iniciar(id -> fail("Sin escucha"))) {
                    assertTimeout(Duration.ofMillis(500), () -> RedCotizaciones.notificar(77));
                    assertTrue(recibido.await(3, TimeUnit.SECONDS));
                } catch (InterruptedException e) { throw new AssertionError(e); }
            });
        }
        RedCotizaciones.notificar(77); // cerrado: no abre conexiones ni lanza excepciones
    }

    @Test void servidorAusenteNoBloqueaAlConsumidor() throws Exception {
        int puerto;
        try (var reserva = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            puerto = reserva.getLocalPort();
        }
        conConfiguracion("0", "" + puerto, () -> {
            try (var red = RedCotizaciones.iniciar(id -> { })) {
                assertTimeout(Duration.ofMillis(500), () -> RedCotizaciones.notificar(1));
            }
        });
    }

    @Test void arranqueConPuertoOcupadoNoDerribaAplicacion() throws Exception {
        try (var servidor = new ServidorCotizaciones(0, id -> { })) {
            conConfiguracion("" + servidor.puerto(), "0", () -> {
                try (var red = RedCotizaciones.iniciar(id -> fail("Puerto ocupado"))) {
                    assertDoesNotThrow(() -> RedCotizaciones.notificar(1));
                }
            });
            ClienteCotizaciones.enviar(servidor.puerto(), 1);
        }
    }

    @Test void configuracionInvalidaDesactivaRedSinFallar() {
        conConfiguracion("texto", "70000", () -> {
            try (var red = RedCotizaciones.iniciar(id -> fail("Desactivada"))) {
                assertDoesNotThrow(() -> RedCotizaciones.notificar(1));
            }
        });
    }
}
