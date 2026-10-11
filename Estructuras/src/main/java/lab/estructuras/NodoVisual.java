package lab.estructuras;

/**
 * Representa un vertice del grafo junto con su posicion en pantalla.
 * Se usa para poder DIBUJAR el grafo: cada vertice tiene un id (0, 1, 2...)
 * y unas coordenadas (x, y) que indican donde pintar su circulo en el lienzo.
 */
public class NodoVisual {

    private int id;   // numero del vertice (coincide con la fila/columna de la matriz)
    private int x;    // coordenada x del centro del circulo
    private int y;    // coordenada y del centro del circulo

    public NodoVisual(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    /** true si el punto (px, py) cae dentro del circulo del vertice. */
    public boolean contiene(int px, int py, int radio) {
        int dx = px - x;
        int dy = py - y;
        return dx * dx + dy * dy <= radio * radio;
    }
}
