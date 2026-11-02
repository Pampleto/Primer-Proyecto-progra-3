package red;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.function.IntConsumer;

/** Ciclo de vida de la red opcional de cada instancia de TravelSafe. */
public final class RedCotizaciones implements AutoCloseable {
    private static volatile RedCotizaciones activa;
    private final int destino;
    private ServidorCotizaciones servidor;
    private final ThreadPoolExecutor envios = new ThreadPoolExecutor(1, 1, 0,
            TimeUnit.SECONDS, new ArrayBlockingQueue<>(32), r -> {
                Thread hilo = new Thread(r, "sockets-envio");
                hilo.setDaemon(true);
                return hilo;
            });

    private RedCotizaciones(int destino) { this.destino = destino; }

    // Se llama desde Application.init(), fuera del hilo JavaFX.
    public static RedCotizaciones iniciar(IntConsumer receptor) {
        RedCotizaciones red = new RedCotizaciones(puerto("travelsafe.sockets.destino"));
        int escucha = puerto("travelsafe.sockets.escucha");
        if (escucha > 0) {
            try {
                red.servidor = new ServidorCotizaciones(escucha, receptor);
                System.out.println("[sockets] Escuchando localhost:" + red.servidor.puerto());
            } catch (IOException e) {
                System.err.println("[sockets] No se pudo iniciar el servidor (puerto ocupado): " + e.getMessage());
            }
        }
        activa = red;
        return red;
    }

    private static int puerto(String propiedad) {
        String valor = System.getProperty(propiedad, "0");
        try {
            int puerto = Integer.parseInt(valor);
            if (puerto >= 0 && puerto <= 65535) return puerto;
        } catch (NumberFormatException ignored) { }
        System.err.println("[sockets] Puerto invalido en " + propiedad + "; desactivado");
        return 0;
    }

    /** Solo encola; un fallo de red nunca revierte el registro JDBC. */
    public static void notificar(int id) {
        RedCotizaciones red = activa;
        if (red == null || red.destino == 0) return;
        try {
            red.envios.execute(() -> {
                try {
                    ClienteCotizaciones.enviar(red.destino, id);
                    System.out.println("[sockets] Enviado " + ProtocoloCotizaciones.mensaje(id));
                } catch (IOException | RuntimeException e) {
                    System.err.println("[sockets] Cotizacion guardada; aviso no entregado: " + e.getMessage());
                }
            });
        } catch (RejectedExecutionException e) {
            System.err.println("[sockets] Cotizacion guardada; cola cerrada o llena");
        }
    }

    @Override
    public void close() {
        if (activa == this) activa = null;
        envios.shutdownNow();
        if (servidor != null) servidor.close();
        try {
            envios.awaitTermination(2500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
