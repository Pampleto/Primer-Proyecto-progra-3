package red;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.IntConsumer;

/** TCP local, una notificacion por conexion. Nunca consulta ni escribe SQL. */
public final class ServidorCotizaciones implements AutoCloseable {
    private final ServerSocket servidor;
    private final Set<Socket> conexiones = ConcurrentHashMap.newKeySet();
    private final ThreadPoolExecutor trabajadores = new ThreadPoolExecutor(2, 2, 0,
            TimeUnit.SECONDS, new ArrayBlockingQueue<>(16), r -> {
                Thread hilo = new Thread(r, "sockets-recepcion");
                hilo.setDaemon(true);
                return hilo;
            });
    private final Thread aceptador;
    private volatile boolean cerrado;

    public ServidorCotizaciones(int puerto, IntConsumer receptor) throws IOException {
        java.util.Objects.requireNonNull(receptor);
        servidor = new ServerSocket();
        try {
            servidor.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), puerto));
        } catch (IOException | RuntimeException e) {
            servidor.close();
            throw e;
        }
        aceptador = new Thread(() -> aceptar(receptor), "sockets-aceptar");
        aceptador.setDaemon(true);
        aceptador.start();
    }

    public int puerto() { return servidor.getLocalPort(); }

    private void aceptar(IntConsumer receptor) {
        while (!cerrado) {
            try {
                Socket cliente = servidor.accept();
                conexiones.add(cliente);
                if (cerrado) {
                    cerrar(cliente);
                    conexiones.remove(cliente);
                    break;
                }
                try {
                    trabajadores.execute(() -> recibir(cliente, receptor));
                } catch (RejectedExecutionException e) {
                    conexiones.remove(cliente);
                    cerrar(cliente);
                }
            } catch (IOException e) {
                if (!cerrado) System.err.println("[sockets] Fallo de aceptacion: " + e.getMessage());
                break;
            }
        }
    }

    private void recibir(Socket cliente, IntConsumer receptor) {
        try (cliente;
             Reader entrada = new InputStreamReader(cliente.getInputStream(), StandardCharsets.UTF_8);
             Writer salida = new OutputStreamWriter(cliente.getOutputStream(), StandardCharsets.UTF_8)) {
            cliente.setSoTimeout(1000);
            long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
            StringBuilder mensaje = new StringBuilder();
            while (true) {
                if (System.nanoTime() > limite) throw new IOException("Tiempo agotado");
                int c = entrada.read();
                if (c == -1) throw new IOException("Mensaje incompleto");
                if (c == '\n') break;
                if (mensaje.length() >= 32) throw new IOException("Mensaje demasiado largo");
                mensaje.append((char) c);
            }
            int id = ProtocoloCotizaciones.validar(mensaje.toString());
            receptor.accept(id);
            salida.write("OK\n");
            salida.flush();
            System.out.println("[sockets] Recibido " + mensaje);
        } catch (IOException | RuntimeException e) {
            if (!cerrado) System.err.println("[sockets] Conexion descartada: " + e.getMessage());
        } finally {
            conexiones.remove(cliente);
        }
    }

    private static void cerrar(Socket socket) {
        try { socket.close(); } catch (IOException ignored) { }
    }

    @Override
    public void close() {
        cerrado = true;
        try { servidor.close(); } catch (IOException ignored) { }
        conexiones.forEach(ServidorCotizaciones::cerrar);
        trabajadores.shutdownNow();
        try {
            aceptador.join(1500);
            trabajadores.awaitTermination(1500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        conexiones.clear();
    }
}
