package lab.estructuras;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * VENTANA PRINCIPAL DEL PROGRAMA (Swing).
 *
 * Organiza todo en una ventana con dos pestañas:
 *   Pestana 1: "Matriz -> Grafo"   (se edita la matriz y se dibuja el grafo)
 *   Pestana 2: "Dibujar grafo -> Matriz" (se dibuja el grafo y se obtiene la matriz)
 *
 * En la parte inferior hay un panel de RESULTADOS que muestra siempre:
 *   - la matriz de adyacencia del grafo actual,
 *   - la lista de adyacencia,
 *   - el recorrido en profundidad (DFS) y en anchura (BFS).
 *
 * Implementa EscuchadorGrafo: por eso se entera cuando el grafo cambia en
 * cualquiera de las dos pestañas y actualiza los resultados.
 */
public class VentanaPrincipal extends JFrame implements EscuchadorGrafo {

    private final JTabbedPane pestanas;              // las dos pestañas
    private final PanelMatrizGrafo panelMatriz;      // contenido de la pestana 1
    private final PanelGrafoMatriz panelGrafo;       // contenido de la pestana 2
    private final JTextArea txtResultados;           // area de texto de resultados
    private final JSpinner spnInicio;                // vertice inicial de los recorridos
    private Grafo grafoActual;                       // ultimo grafo recibido

    public VentanaPrincipal() {
        setTitle("Grafos: Matriz de Adyacencia <-> Grafo (DFS / BFS)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 760);
        setLocationRelativeTo(null);                 // centra la ventana en la pantalla
        setLayout(new BorderLayout(6, 6));

        // ---------- 1) Construir las dos pestañas ----------
        panelMatriz = new PanelMatrizGrafo(this);
        panelGrafo = new PanelGrafoMatriz(this);

        pestanas = new JTabbedPane();
        pestanas.addTab("1. Matriz -> Grafo", panelMatriz);
        pestanas.addTab("2. Dibujar grafo -> Matriz", panelGrafo);
        add(pestanas, BorderLayout.CENTER);

        // ---------- 2) Panel inferior de resultados ----------
        JPanel sur = new JPanel(new BorderLayout(6, 6));
        sur.setBorder(BorderFactory.createTitledBorder("Resultados del grafo actual"));
        sur.setPreferredSize(new Dimension(1080, 230));

        // Controles: vertice inicial de los recorridos + boton para recalcular
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        controles.add(new JLabel("Vertice inicial de los recorridos:"));
        spnInicio = new JSpinner(new SpinnerNumberModel(0, 0, 0, 1));
        controles.add(spnInicio);

        JButton btnRecorridos = new JButton("Calcular recorridos");
        controles.add(btnRecorridos);
        sur.add(controles, BorderLayout.NORTH);

        // Area de texto donde se escribe todo el analisis del grafo
        txtResultados = new JTextArea();
        txtResultados.setEditable(false);
        txtResultados.setFont(new Font("Monospaced", Font.PLAIN, 13));
        sur.add(new JScrollPane(txtResultados), BorderLayout.CENTER);

        add(sur, BorderLayout.SOUTH);

        // ---------- 3) Eventos ----------
        // Cada vez que se cambia de pestana, se recalcula con el grafo de esa pestana
        pestanas.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Grafo g = grafoDePestanaActual();
                if (g != null) {
                    grafoModificado(g);
                }
            }
        });

        btnRecorridos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarResultados();      // recalcula usando el vertice elegido
            }
        });

        // ---------- 4) Primer calculo ----------
        // Al terminar de armar la ventana, mostramos los resultados iniciales
        Grafo g = grafoDePestanaActual();
        if (g != null) {
            grafoModificado(g);
        }
    }

    /** Devuelve el grafo de la pestana que esta visible. */
    private Grafo grafoDePestanaActual() {
        if (pestanas.getSelectedIndex() == 0) {
            return panelMatriz.getGrafoActual();
        }
        return panelGrafo.getGrafo();
    }

    // ------------------------------------------------------------------
    // Metodo de la interfaz EscuchadorGrafo
    // ------------------------------------------------------------------

    /**
     * Lo llaman las pestañas cuando el grafo cambia. Guarda el grafo, ajusta el
     * limite del selector de vertice inicial y refresca los resultados.
     */
    @Override
    public void grafoModificado(Grafo grafo) {
        this.grafoActual = grafo;
        // Durante la construccion el panel de abajo aun no existe: solo guardar
        if (spnInicio == null || txtResultados == null) {
            return;
        }
        int n = grafo.getN();
        int actual = (Integer) spnInicio.getValue();
        if (n == 0) {
            spnInicio.setModel(new SpinnerNumberModel(0, 0, 0, 1));
        } else {
            if (actual > n - 1) {
                actual = n - 1;          // evita dejar seleccionado un vertice inexistente
            }
            spnInicio.setModel(new SpinnerNumberModel(actual, 0, n - 1, 1));
        }
        mostrarResultados();
    }

    /** Escribe matriz, lista de adyacencia, DFS y BFS en el area de resultados. */
    private void mostrarResultados() {
        if (grafoActual == null) {
            return;
        }
        int inicio = 0;
        Object valor = spnInicio.getValue();
        if (valor instanceof Integer) {
            inicio = (Integer) valor;
        }
        if (inicio >= grafoActual.getN()) {
            inicio = 0;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== MATRIZ DE ADYACENCIA (tamano ").append(grafoActual.getN())
                .append(" x ").append(grafoActual.getN()).append(") ===\n");
        sb.append(grafoActual.matrizComoTexto()).append('\n');

        sb.append("=== LISTA DE ADYACENCIA ===\n");
        sb.append(grafoActual.listaAdyacenciaComoTexto()).append('\n');

        sb.append("=== RECORRIDO EN PROFUNDIDAD (DFS) desde V").append(inicio).append(" ===\n");
        sb.append(Grafo.recorridoComoTexto(grafoActual.recorridoProfundidad(inicio))).append('\n');

        sb.append("=== RECORRIDO EN ANCHURA (BFS) desde V").append(inicio).append(" ===\n");
        sb.append(Grafo.recorridoComoTexto(grafoActual.recorridoAnchura(inicio))).append('\n');

        txtResultados.setText(sb.toString());
        txtResultados.setCaretPosition(0);       // sube el scroll al principio
    }

    // ------------------------------------------------------------------
    // Punto de entrada
    // ------------------------------------------------------------------

    public static void main(String[] args) {
        // Todo lo grafico de Swing debe crearse en el hilo de eventos (EDT)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new VentanaPrincipal().setVisible(true);
            }
        });
    }
}

