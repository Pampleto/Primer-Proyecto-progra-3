package red;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/** Envio acotado a localhost; llamar fuera del hilo JavaFX. */
public final class ClienteCotizaciones {
    private ClienteCotizaciones() { }

    public static void enviar(int puerto, int id) throws IOException {
        String mensaje = ProtocoloCotizaciones.mensaje(id);
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), puerto), 750);
            socket.setSoTimeout(1000);
            try (Writer salida = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
                 BufferedReader entrada = new BufferedReader(new InputStreamReader(
                         socket.getInputStream(), StandardCharsets.UTF_8))) {
                salida.write(mensaje + "\n");
                salida.flush();
                // ACK fijo: no se usa readLine sin limite para respuestas remotas.
                for (char esperado : "OK\n".toCharArray()) {
                    if (entrada.read() != esperado) throw new IOException("Sin confirmacion del servidor");
                }
            }
        }
    }
}
