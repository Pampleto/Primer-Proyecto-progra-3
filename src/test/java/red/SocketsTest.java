package red;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(20)
class SocketsTest {
    @Test void servidorClienteMensajeCompletoYAck() throws Exception {
        AtomicInteger recibido = new AtomicInteger();
        try (var servidor = new ServidorCotizaciones(0, recibido::set)) {
            assertTrue(servidor.puerto() > 0);
            ClienteCotizaciones.enviar(servidor.puerto(), 42);
            assertEquals(42, recibido.get());
        }
    }

    @Test void validaProtocoloSinDatosAdicionales() {
        assertEquals(123, ProtocoloCotizaciones.validar("COTIZACION_CREADA|123"));
        assertEquals("COTIZACION_CREADA|123", ProtocoloCotizaciones.mensaje(123));
        for (String texto : new String[]{"", "CHAT|1", "COTIZACION_CREADA|0",
                "COTIZACION_CREADA|-1", "COTIZACION_CREADA|2147483648",
                "COTIZACION_CREADA|1|secreto", "COTIZACION_CREADA|01"}) {
            assertThrows(IllegalArgumentException.class, () -> ProtocoloCotizaciones.validar(texto));
        }
        assertThrows(IllegalArgumentException.class, () -> ProtocoloCotizaciones.validar(null));
        assertThrows(IllegalArgumentException.class, () -> ProtocoloCotizaciones.mensaje(0));
    }

    private static Socket conectar(int puerto) throws IOException {
        Socket socket = new Socket(InetAddress.getLoopbackAddress(), puerto);
        socket.setSoTimeout(4000);
        return socket;
    }

    @Test void invalidosLargosEIncompletosNoDerribanServidor() throws Exception {
        AtomicInteger recibidos = new AtomicInteger();
        try (var servidor = new ServidorCotizaciones(0, id -> recibidos.incrementAndGet())) {
            for (String mensaje : new String[]{"CHAT|2\n", "x".repeat(100) + "\n", "COTIZACION_CREADA|2"}) {
                try (Socket socket = conectar(servidor.puerto())) {
                    socket.getOutputStream().write(mensaje.getBytes(StandardCharsets.UTF_8));
                    socket.shutdownOutput();
                    assertEquals(-1, socket.getInputStream().read());
                }
            }
            assertEquals(0, recibidos.get());
            ClienteCotizaciones.enviar(servidor.puerto(), 2);
            assertEquals(1, recibidos.get());
        }
    }

    @Test void tcpReconstruyeMensajeFragmentado() throws Exception {
        AtomicInteger recibido = new AtomicInteger();
        try (var servidor = new ServidorCotizaciones(0, recibido::set);
             Socket socket = conectar(servidor.puerto())) {
            for (byte b : "COTIZACION_CREADA|55\n".getBytes(StandardCharsets.UTF_8)) {
                socket.getOutputStream().write(b);
                socket.getOutputStream().flush();
            }
            assertEquals('O', socket.getInputStream().read());
            assertEquals(55, recibido.get());
        }
    }

    @Test void puertoOcupadoSeReportaSinAfectarAlOriginal() throws Exception {
        try (var servidor = new ServidorCotizaciones(0, id -> { })) {
            assertThrows(BindException.class, () -> new ServidorCotizaciones(servidor.puerto(), id -> { }));
            ClienteCotizaciones.enviar(servidor.puerto(), 1);
        }
    }

    @Test void servidorAusenteFallaEnTiempoAcotado() throws Exception {
        int puerto;
        try (var reserva = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            puerto = reserva.getLocalPort();
        }
        assertTimeoutPreemptively(Duration.ofSeconds(3), () ->
                assertThrows(IOException.class, () -> ClienteCotizaciones.enviar(puerto, 1)));
    }

    @Test void desconexionYClienteSilenciosoNoImpidenOtrosEnvios() throws Exception {
        try (var servidor = new ServidorCotizaciones(0, id -> { });
             Socket silencioso = conectar(servidor.puerto())) {
            try (Socket desconectado = conectar(servidor.puerto())) {
                desconectado.shutdownOutput();
            }
            ClienteCotizaciones.enviar(servidor.puerto(), 1);
            assertEquals(-1, silencioso.getInputStream().read());
        }
    }

    @Test void cierreLiberaPuertoYConexionesActivas() throws Exception {
        var servidor = new ServidorCotizaciones(0, id -> { });
        int puerto = servidor.puerto();
        try (Socket socket = conectar(puerto)) {
            assertTimeoutPreemptively(Duration.ofSeconds(4), servidor::close);
            assertEquals(-1, socket.getInputStream().read());
            servidor.close();
            try (var nuevo = new ServidorCotizaciones(puerto, id -> { })) {
                ClienteCotizaciones.enviar(nuevo.puerto(), 1);
            }
        } finally { servidor.close(); }
    }

    private static Process proceso(String... argumentos) throws IOException {
        var comando = new java.util.ArrayList<String>();
        comando.add(Path.of(System.getProperty("java.home"), "bin",
                System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java").toString());
        comando.add("-cp");
        comando.add(Path.of("target/classes").toAbsolutePath() + File.pathSeparator
                + Path.of("target/test-classes").toAbsolutePath());
        comando.add("red.ProcesoSockets");
        comando.addAll(java.util.List.of(argumentos));
        return new ProcessBuilder(comando).redirectErrorStream(true).start();
    }

    @Test void dosProcesosJavaIndependientesIntercambianMensaje() throws Exception {
        Process servidor = proceso("servidor");
        Process cliente = null;
        try (var lector = servidor.inputReader(StandardCharsets.UTF_8)) {
            String listo = CompletableFuture.supplyAsync(() -> {
                try { return lector.readLine(); }
                catch (IOException e) { throw new UncheckedIOException(e); }
            }).get(5, TimeUnit.SECONDS);
            assertTrue(listo.startsWith("PUERTO="), listo);
            cliente = proceso("cliente", listo.substring(7));
            assertTrue(cliente.waitFor(6, TimeUnit.SECONDS));
            String salidaCliente = new String(cliente.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(0, cliente.exitValue(), salidaCliente);
            assertTrue(salidaCliente.contains("ENVIADO"));
            servidor.getOutputStream().write('x');
            servidor.getOutputStream().flush();
            assertTrue(servidor.waitFor(5, TimeUnit.SECONDS));
            String salidaServidor = lector.lines().collect(java.util.stream.Collectors.joining("\n"));
            assertEquals(0, servidor.exitValue(), salidaServidor);
            assertTrue(salidaServidor.contains("ID=123"), salidaServidor);
            assertTrue(salidaServidor.contains("COTIZACION_CREADA|123"), salidaServidor);
            System.out.println("DOS JVM: " + salidaCliente.trim() + " / " + salidaServidor);
        } finally {
            servidor.destroyForcibly();
            if (cliente != null) cliente.destroyForcibly();
        }
    }
}
