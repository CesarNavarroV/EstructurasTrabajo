package lab.estructuras;

/**
 * LAUNCHER (punto de entrada del programa).
 *
 * Simplemente arranca la ventana principal hecha con Swing.
 * Se recomienda ejecutar esta clase (Run 'Launcher.main()').
 */
public class Launcher {

    public static void main(String[] args) {
        // Delega en VentanaPrincipal, que crea la interfaz en el hilo de Swing
        VentanaPrincipal.main(args);
    }
}
