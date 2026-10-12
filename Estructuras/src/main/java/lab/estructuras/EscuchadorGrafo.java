package lab.estructuras;

/**
 * Interfaz "escuchador": permite avisar a la ventana principal cada vez que el
 * grafo cambia (al editar la matriz, al dibujar, al generar la matriz, etc.),
 * para que se vuelvan a calcular la lista de adyacencia y los recorridos.
 */
public interface EscuchadorGrafo {

    void grafoModificado(Grafo grafo);
}
