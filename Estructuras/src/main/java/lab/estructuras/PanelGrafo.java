package lab.estructuras;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.QuadCurve2D;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * LIENZO DEL GRAFO (componente visual hecho con Swing puro, heredando de JPanel).
 *
 * Sirve para dos cosas:
 *  1) DIBUJAR el grafo (flechas, circulos y pesos) en la opcion 1.
 *  2) DEJAR DIBUJAR al usuario con el mouse (opcion 2): segun el "modo"
 *     activo, el clic agrega vertices, los une, los mueve o los borra.
 */
public class PanelGrafo extends JPanel {

    /** Modos de edicion del lienzo. */
    public enum Modo {
        VERTICE,          // clic en zona vacia -> agrega un vertice
        ARISTA,           // clic en un vertice y luego en otro -> crea arista
        MOVER,            // arrastrar un vertice -> cambia su posicion
        BORRAR_VERTICE,   // clic sobre un vertice -> lo elimina
        BORRAR_ARISTA     // clic en un vertice y luego en otro -> elimina la arista
    }

    private static final int RADIO = 20;   // radio del circulo de cada vertice

    private Grafo grafo;                          // grafo que se pinta
    private Modo modo = Modo.VERTICE;             // modo de edicion actual
    private EscuchadorGrafo escuchador;           // a quien avisar cuando hay cambios
    private Integer primerClic = null;            // vertice marcado al crear/borrar aristas
    private int idArrastrado = -1;                // vertice que se esta arrastrando

    public PanelGrafo(Grafo grafo, EscuchadorGrafo escuchador) {
        this.grafo = grafo;
        this.escuchador = escuchador;
        setBackground(new Color(0xF7, 0xF9, 0xFB));           // fondo claro
        addMouseListener(new ManejadorRaton());               // clics del usuario
        addMouseMotionListener(new ManejadorRaton());         // arrastre de vertices
    }

    public Grafo getGrafo() {
        return grafo;
    }

    public void setGrafo(Grafo grafo) {
        this.grafo = grafo;
        primerClic = null;
        repaint();
    }

    public void setModo(Modo modo) {
        this.modo = modo;
        primerClic = null;      // al cambiar de modo se descarta la seleccion a medias
        repaint();
    }

    public Modo getModo() {
        return modo;
    }

    // ------------------------------------------------------------------
    // DIBUJO
    // ------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // Suavizado para que las lineas y los circulos se vean bien
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        List<NodoVisual> nodos = grafo.getNodos();

        dibujarAristas(g2, nodos);   // primero las aristas (quedan debajo de los vertices)
        dibujarVertices(g2, nodos);  // despues los vertices encima

        // Si no hay vertices, mostramos una ayuda en el centro del lienzo
        if (nodos.isEmpty()) {
            g2.setColor(new Color(0x8A, 0x94, 0xA6));
            g2.setFont(new Font("SansSerif", Font.ITALIC, 14));
            g2.drawString("Dibuja aqui: agrega vertices y unelos con aristas.",
                    60, getHeight() / 2);
        }
    }

    /** Dibuja todas las aristas del grafo (lineas, con flecha si es dirigido). */
    private void dibujarAristas(Graphics2D g2, List<NodoVisual> nodos) {
        g2.setStroke(new BasicStroke(2f));
        for (int i = 0; i < grafo.getN(); i++) {
            for (int j = 0; j < grafo.getN(); j++) {
                int valor = grafo.getValor(i, j);
                if (valor == 0 || i >= nodos.size() || j >= nodos.size()) {
                    continue;
                }
                NodoVisual a = nodos.get(i);
                NodoVisual b = nodos.get(j);

                if (i == j) {
                    dibujarBucle(g2, a, valor);            // arista de un vertice a si mismo
                    continue;
                }
                if (!grafo.isDirigido() && j < i) {
                    continue;                              // no dirigido: dibujar una sola vez
                }

                boolean doble = grafo.isDirigido()      // i->j y j->i a la vez: usar curva
                        && grafo.getValor(j, i) != 0 && j < i;

                if (doble) {
                    dibujarAristaCurva(g2, a, b, valor);
                } else {
                    dibujarAristaRecta(g2, a, b, valor);
                }
            }
        }
    }

    /** Arista en linea recta, recortada en el borde de los circulos. */
    private void dibujarAristaRecta(Graphics2D g2, NodoVisual a, NodoVisual b, int valor) {
        int x1 = a.getX();
        int y1 = a.getY();
        int x2 = b.getX();
        int y2 = b.getY();

        double angulo = Math.atan2(y2 - y1, x2 - x1);
        int inicioX = (int) Math.round(x1 + RADIO * Math.cos(angulo));
        int inicioY = (int) Math.round(y1 + RADIO * Math.sin(angulo));
        int finX = (int) Math.round(x2 - RADIO * Math.cos(angulo));
        int finY = (int) Math.round(y2 - RADIO * Math.sin(angulo));

        g2.setColor(new Color(0x50, 0x5A, 0x6E));
        g2.drawLine(inicioX, inicioY, finX, finY);

        if (grafo.isDirigido()) {
            dibujarPuntaFlecha(g2, finX, finY, angulo);
        }
        if (grafo.isPonderado()) {
            dibujarPeso(g2, (inicioX + finX) / 2, (inicioY + finY) / 2, valor);
        }
    }

    /** Arista curvada, usada cuando existen aristas en ambos sentidos. */
    private void dibujarAristaCurva(Graphics2D g2, NodoVisual a, NodoVisual b, int valor) {
        int x1 = a.getX();
        int y1 = a.getY();
        int x2 = b.getX();
        int y2 = b.getY();

        // Punto medio desplazado hacia un lado: control de la curva de Bezier
        double mx = (x1 + x2) / 2.0;
        double my = (y1 + y2) / 2.0;
        double dx = x2 - x1;
        double dy = y2 - y1;
        double largo = Math.sqrt(dx * dx + dy * dy);
        double desplazamiento = Math.min(35, largo / 4);
        double cx = mx - dy / largo * desplazamiento;
        double cy = my + dx / largo * desplazamiento;

        g2.setColor(new Color(0x50, 0x5A, 0x6E));
        g2.draw(new QuadCurve2D.Double(x1, y1, cx, cy, x2, y2));

        if (grafo.isDirigido()) {
            double angulo = Math.atan2(y2 - cy, x2 - cx);
            int finX = (int) Math.round(x2 - RADIO * Math.cos(angulo));
            int finY = (int) Math.round(y2 - RADIO * Math.sin(angulo));
            dibujarPuntaFlecha(g2, finX, finY, angulo);
        }
        if (grafo.isPonderado()) {
            dibujarPeso(g2, (int) cx, (int) cy, valor);
        }
    }

    /** Dibuja un bucle (arista de un vertice hacia si mismo). */
    private void dibujarBucle(Graphics2D g2, NodoVisual a, int valor) {
        int x = a.getX();
        int y = a.getY() - RADIO - 12;
        g2.setColor(new Color(0x50, 0x5A, 0x6E));
        g2.drawOval(x - 12, y - 12, 24, 24);
        if (grafo.isDirigido()) {
            dibujarPuntaFlecha(g2, x - 6, y + 6, Math.PI / 4);
        }
        if (grafo.isPonderado()) {
            dibujarPeso(g2, x + 20, y - 12, valor);
        }
    }

    /** Dibuja la punta de flecha en (x, y) con el angulo indicado. */
    private void dibujarPuntaFlecha(Graphics2D g2, int x, int y, double angulo) {
        int tam = 12;
        int ax = (int) Math.round(x - tam * Math.cos(angulo - Math.PI / 7));
        int ay = (int) Math.round(y - tam * Math.sin(angulo - Math.PI / 7));
        int bx = (int) Math.round(x - tam * Math.cos(angulo + Math.PI / 7));
        int by = (int) Math.round(y - tam * Math.sin(angulo + Math.PI / 7));
        g2.drawLine(x, y, ax, ay);
        g2.drawLine(x, y, bx, by);
    }

    /** Dibuja el peso de la arista con un fondo blanco para que se lea. */
    private void dibujarPeso(Graphics2D g2, int x, int y, int valor) {
        String texto = String.valueOf(valor);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        int ancho = g2.getFontMetrics().stringWidth(texto);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x - ancho / 2 - 3, y - 9, ancho + 6, 16, 6, 6);
        g2.setColor(new Color(0xC0, 0x39, 0x2B));
        g2.drawString(texto, x - ancho / 2, y + 4);
    }

    /** Dibuja los circulos de los vertices con su etiqueta V0, V1... */
    private void dibujarVertices(Graphics2D g2, List<NodoVisual> nodos) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        for (int i = 0; i < nodos.size(); i++) {
            NodoVisual n = nodos.get(i);
            boolean marcado = primerClic != null && primerClic == n.getId();

            Color relleno = marcado ? new Color(0xF3, 0x9C, 0x12) : new Color(0x2E, 0x86, 0xC1);
            g2.setColor(relleno);
            g2.fillOval(n.getX() - RADIO, n.getY() - RADIO, RADIO * 2, RADIO * 2);

            g2.setColor(Color.WHITE);
            g2.drawOval(n.getX() - RADIO, n.getY() - RADIO, RADIO * 2, RADIO * 2);

            String etiqueta = "V" + n.getId();
            int ancho = g2.getFontMetrics().stringWidth(etiqueta);
            g2.drawString(etiqueta, n.getX() - ancho / 2, n.getY() + 5);
        }
    }


    // ------------------------------------------------------------------
    // INTERACCION CON EL MOUSE
    // ------------------------------------------------------------------

    /** Devuelve el id del vertice que esta bajo el punto (x, y), o -1 si no hay. */
    private int verticeEn(int x, int y) {
        List<NodoVisual> nodos = grafo.getNodos();
        for (int i = nodos.size() - 1; i >= 0; i--) {
            if (nodos.get(i).contiene(x, y, RADIO)) {
                return nodos.get(i).getId();
            }
        }
        return -1;
    }

    /** Pide al usuario el peso de una arista (solo si el grafo es ponderado). */
    private int pedirPeso(int origen, int destino) {
        String resp = JOptionPane.showInputDialog(this,
                "Peso de la arista V" + origen + " -> V" + destino + ":",
                "1");
        if (resp == null) {
            return 1;                                    // cancelado: peso 1 por defecto
        }
        try {
            return Integer.parseInt(resp.trim());
        } catch (NumberFormatException ex) {
            return 1;                                    // dato no numerico: peso 1
        }
    }

    /** Avisa a la ventana principal de que el grafo cambio. */
    private void notificarCambio() {
        if (escuchador != null) {
            escuchador.grafoModificado(grafo);
        }
    }

    /**
     * Clase interna que maneja todos los eventos del mouse sobre el lienzo.
     * Es la que permite "dibujar el grafo" con el mouse en la opcion 2.
     */
    private class ManejadorRaton extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            int id = verticeEn(e.getX(), e.getY());

            switch (modo) {
                case VERTICE:
                    // Clic en zona vacia -> agregar vertice nuevo ahi
                    if (id == -1) {
                        grafo.agregarVertice(e.getX(), e.getY());
                        notificarCambio();
                    }
                    break;

                case ARISTA:
                    // Primer clic: marca vertice origen. Segundo clic: crea la arista
                    if (id == -1) {
                        primerClic = null;
                    } else if (primerClic == null) {
                        primerClic = id;
                    } else if (primerClic != id) {
                        int peso = grafo.isPonderado() ? pedirPeso(primerClic, id) : 1;
                        grafo.agregarArista(primerClic, id, peso);
                        primerClic = null;
                        notificarCambio();
                    }
                    break;

                case BORRAR_ARISTA:
                    // Igual que ARISTA pero eliminando
                    if (id != -1 && primerClic == null) {
                        primerClic = id;
                    } else if (id != -1 && primerClic != null && primerClic != id) {
                        grafo.eliminarArista(primerClic, id);
                        primerClic = null;
                        notificarCambio();
                    }
                    break;

                case MOVER:
                    // Empezar a arrastrar el vertice donde se hizo clic
                    idArrastrado = id;
                    break;

                case BORRAR_VERTICE:
                    // Clic sobre un vertice -> eliminarlo
                    if (id != -1) {
                        grafo.eliminarVertice(id);
                        primerClic = null;
                        notificarCambio();
                    }
                    break;

                default:
                    break;
            }
            repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            // En modo MOVER: seguir la posicion del mouse (sin salirse del lienzo)
            if (modo == Modo.MOVER && idArrastrado != -1) {
                for (NodoVisual n : grafo.getNodos()) {
                    if (n.getId() == idArrastrado) {
                        int x = Math.max(RADIO, Math.min(getWidth() - RADIO, e.getX()));
                        int y = Math.max(RADIO, Math.min(getHeight() - RADIO, e.getY()));
                        n.setX(x);
                        n.setY(y);
                        break;
                    }
                }
                repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // Al soltar el mouse termina el arrastre y se avisa del cambio
            if (modo == Modo.MOVER && idArrastrado != -1) {
                idArrastrado = -1;
                notificarCambio();
            }
        }
    }
}

