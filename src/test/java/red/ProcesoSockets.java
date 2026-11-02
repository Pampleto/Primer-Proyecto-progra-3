package red;

import java.util.concurrent.*;

/** Auxiliar ejecutado por JUnit en JVM independientes, sin JavaFX ni MySQL. */
public final class ProcesoSockets {
    public static void main(String[] args) throws Exception {
        if (args[0].equals("cliente")) {
            ClienteCotizaciones.enviar(Integer.parseInt(args[1]), 123);
            System.out.println("ENVIADO");
        } else {
            CountDownLatch recibido = new CountDownLatch(1);
            try (ServidorCotizaciones servidor = new ServidorCotizaciones(0, id -> {
                System.out.println("ID=" + id);
                recibido.countDown();
            })) {
                System.out.println("PUERTO=" + servidor.puerto());
                if (!recibido.await(10, TimeUnit.SECONDS)) throw new AssertionError("Sin mensaje");
                // Permite que el trabajador envie el ACK antes de cerrar.
                if (System.in.read() != 'x') throw new AssertionError("Sin cierre coordinado");
            }
        }
    }
}
