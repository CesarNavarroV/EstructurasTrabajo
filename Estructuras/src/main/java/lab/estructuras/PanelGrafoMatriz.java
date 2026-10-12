package lab.estructuras;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * OPCION 2: DIBUJAR GRAFO -> MATRIZ.
 *
 * El usuario dibuja el grafo con el mouse sobre el lienzo (agrega vertices,
 * los une con aristas, los mueve o los borra) y al pulsar "Generar matriz"
 * el programa escribe la matriz de adyacencia correspondiente.
 */
public class PanelGrafoMatriz extends JPanel {

    // ------------------------------------------------------------------
    // Componentes
    // ------------------------------------------------------------------
    private final EscuchadorGrafo escuchador;   // avisa a la ventana principal
    private final PanelGrafo lienzo;            // zona donde se dibuja el grafo
    private final Grafo grafo;                  // grafo que se esta dibujando
    private final JCheckBox chkDirigido;
    private final JCheckBox chkPonderado;
    private final JTextArea txtMatriz;          // matriz resultante
    private final JLabel lblAyuda;              // explica que hace cada modo

    public PanelGrafoMatriz(EscuchadorGrafo escuchador) {
        super(new BorderLayout(6, 6));
        this.escuchador = escuchador;

        // ---------- 1) Grafo vacio y lienzo para dibujarlo ----------
        grafo = new Grafo(0, false, false);
        lienzo = new PanelGrafo(grafo, escuchador);
        lienzo.setModo(PanelGrafo.Modo.VERTICE);

        // ---------- 2) Barra de herramientas (modos de dibujo) ----------
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));

        JRadioButton rbVertice = new JRadioButton("Agregar vertice", true);
        JRadioButton rbArista = new JRadioButton("Unir (arista)");
        JRadioButton rbMover = new JRadioButton("Mover vertice");
        JRadioButton rbBorrarV = new JRadioButton("Borrar vertice");
        JRadioButton rbBorrarA = new JRadioButton("Borrar arista");

        // Un solo modo activo a la vez
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbVertice);
        grupo.add(rbArista);
        grupo.add(rbMover);
        grupo.add(rbBorrarV);
        grupo.add(rbBorrarA);
        barra.add(new JLabel("Modo:"));
        barra.add(rbVertice);
        barra.add(rbArista);
        barra.add(rbMover);
        barra.add(rbBorrarV);
        barra.add(rbBorrarA);

        // Opciones del grafo y botones de accion
        chkDirigido = new JCheckBox("Dirigido");
        chkPonderado = new JCheckBox("Ponderado");
        barra.add(chkDirigido);
        barra.add(chkPonderado);

        JButton btnMatriz = new JButton("Generar matriz de adyacencia");
        JButton btnLimpiar = new JButton("Limpiar dibujo");
        barra.add(btnMatriz);
        barra.add(btnLimpiar);
        add(barra, BorderLayout.NORTH);

        // ---------- 3) Lienzo centrado (con su mensaje de ayuda) ----------
        JPanel centro = new JPanel(new BorderLayout());
        lblAyuda = new JLabel("  Clic en una zona vacia para crear un vertice.");
        lblAyuda.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        centro.add(lblAyuda, BorderLayout.NORTH);
        centro.add(lienzo, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        // ---------- 4) Matriz resultante a la derecha ----------
        txtMatriz = new JTextArea(14, 22);
        txtMatriz.setEditable(false);                       // es solo de lectura
        txtMatriz.setFont(new Font("Monospaced", Font.BOLD, 13));
        JScrollPane scrollMatriz = new JScrollPane(txtMatriz);
        scrollMatriz.setBorder(BorderFactory.createTitledBorder(
                "Matriz de adyacencia generada"));
        add(scrollMatriz, BorderLayout.EAST);

        // ---------- 5) Eventos de los modos de dibujo ----------
        rbVertice.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lienzo.setModo(PanelGrafo.Modo.VERTICE);
                lblAyuda.setText("  Clic en una zona vacia para crear un vertice.");
            }
        });
        rbArista.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lienzo.setModo(PanelGrafo.Modo.ARISTA);
                lblAyuda.setText("  Clic sobre un vertice y luego sobre otro para unirlos.");
            }
        });
        rbMover.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lienzo.setModo(PanelGrafo.Modo.MOVER);
                lblAyuda.setText("  Arrastre un vertice para cambiar su posicion.");
            }
        });
        rbBorrarV.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lienzo.setModo(PanelGrafo.Modo.BORRAR_VERTICE);
                lblAyuda.setText("  Clic sobre un vertice para eliminarlo.");
            }
        });
        rbBorrarA.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lienzo.setModo(PanelGrafo.Modo.BORRAR_ARISTA);
                lblAyuda.setText("  Clic sobre un vertice y luego sobre otro para borrar la arista.");
            }
        });

        // Cambiar dirigido/ponderado afecta como se guardan las aristas
        chkDirigido.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ajustarTipoGrafo();
            }
        });
        chkPonderado.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ajustarTipoGrafo();
            }
        });

        // Generar matriz: escribe la matriz del grafo dibujado
        btnMatriz.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generarMatriz();
            }
        });

        // Limpiar dibujo: borra todo el lienzo y la matriz
        btnLimpiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                grafo.limpiar();
                txtMatriz.setText("");
                lienzo.repaint();
                escuchador.grafoModificado(grafo);
            }
        });
    }

    /**
     * Ajusta el grafo cuando el usuario marca/desmarca "Dirigido" o "Ponderado".
     * Si pasa a NO dirigido, se simetriza la matriz; si pasa a NO ponderado,
     * todos los pesos se convierten en 1.
     */
    private void ajustarTipoGrafo() {
        grafo.setTipo(chkDirigido.isSelected(), chkPonderado.isSelected());
        lienzo.repaint();
        generarMatriz();
    }

    /** Escribe en el panel la matriz de adyacencia del grafo dibujado. */
    private void generarMatriz() {
        txtMatriz.setText(grafo.matrizComoTexto());
        txtMatriz.setCaretPosition(0);
        escuchador.grafoModificado(grafo);
    }

    /** Devuelve el grafo dibujado (lo usa la ventana principal). */
    public Grafo getGrafo() {
        return grafo;
    }
}

