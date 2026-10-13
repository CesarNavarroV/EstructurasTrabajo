package lab.estructuras;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * OPCION 1: MATRIZ -> GRAFO.
 *
 * El usuario elige el tamaño del grafo y edita TODAS las casillas de la matriz
 * de adyacencia; al convertir, el programa dibuja el grafo correspondiente y
 * muestra la lista de adyacencia y los recorridos.
 */
public class PanelMatrizGrafo extends JPanel {

    // ------------------------------------------------------------------
    // Componentes de la interfaz
    // ------------------------------------------------------------------
    private final EscuchadorGrafo escuchador;   // para avisar a la ventana principal
    private final JSpinner spnN;                // numero de vertices
    private final JCheckBox chkDirigido;        // dirigido o no
    private final JCheckBox chkPonderado;       // con pesos o no
    private JTable tabla;                       // matriz editable
    private DefaultTableModel modelo;           // datos de la tabla
    private Grafo grafoActual;                  // grafo generado desde la matriz

    public PanelMatrizGrafo(EscuchadorGrafo escuchador) {
        super(new BorderLayout(8, 8));
        this.escuchador = escuchador;

        // ---------- 1) Barra superior de controles ----------
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        barra.add(new JLabel("Numero de vertices (n):"));

        spnN = new JSpinner(new SpinnerNumberModel(4, 1, 12, 1));
        barra.add(spnN);

        chkDirigido = new JCheckBox("Dirigido");
        chkPonderado = new JCheckBox("Ponderado");
        barra.add(chkDirigido);
        barra.add(chkPonderado);

        JButton btnGenerar = new JButton("Generar / actualizar matriz");
        JButton btnConvertir = new JButton("Convertir a grafo");
        JButton btnLimpiar = new JButton("Limpiar valores");
        JButton btnEjemplo = new JButton("Cargar ejemplo");
        barra.add(btnGenerar);
        barra.add(btnConvertir);
        barra.add(btnLimpiar);
        barra.add(btnEjemplo);
        add(barra, BorderLayout.NORTH);

        // ---------- 2) Tabla (matriz de adyacencia editable) ----------
        crearTabla(4);                                   // matriz inicial de 4x4
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(javax.swing.BorderFactory.createTitledBorder(
                "Matriz de adyacencia (edite cualquier casilla: 0 = sin arista)"));
        add(scroll, BorderLayout.CENTER);

        // ---------- 3) Eventos ----------
        // Cambiar n: regenera la tabla vacia del tamaño indicado
        spnN.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                crearTabla((Integer) spnN.getValue());
            }
        });

        // Convertir: lee la tabla, arma el grafo y avisa a la ventana
        btnConvertir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                convertir();
            }
        });

        // Generar: vuelve a crear la matriz (por si se cambio n o el tipo de grafo)
        btnGenerar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                crearTabla((Integer) spnN.getValue());
                convertir();
            }
        });

        // Limpiar: deja toda la matriz en 0
        btnLimpiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < modelo.getRowCount(); i++) {
                    for (int j = 1; j < modelo.getColumnCount(); j++) {
                        modelo.setValueAt(Integer.valueOf(0), i, j);
                    }
                }
                convertir();
            }
        });

        // Ejemplo: carga una matriz de 5 vertices lista para probar
        btnEjemplo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cargarEjemplo();
            }
        });

        // Al principio ya tenemos un grafo valido (vacio) para no tener nulos
        grafoActual = new Grafo(4, chkDirigido.isSelected(), chkPonderado.isSelected());
        escuchador.grafoModificado(grafoActual);
    }

    // ------------------------------------------------------------------
    // Creacion de la tabla
    // ------------------------------------------------------------------

    /**
     * Crea (o vuelve a crear) la tabla de la matriz.
     * La columna 0 guarda la etiqueta de la fila (V0, V1...) y no es editable.
     */
    private void crearTabla(int n) {
        String[] encabezados = new String[n + 1];
        encabezados[0] = "";
        for (int j = 0; j < n; j++) {
            encabezados[j + 1] = "V" + j;                 // nombres de las columnas
        }

        modelo = new DefaultTableModel(encabezados, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                // Solo se editan las casillas de datos, no las etiquetas
                return columna > 0;
            }

            @Override
            public Class<?> getColumnClass(int columna) {
                return Integer.class;                     // las celdas guardan enteros
            }
        };

        for (int i = 0; i < n; i++) {
            Object[] fila = new Object[n + 1];
            fila[0] = "V" + i;                            // etiqueta de la fila
            for (int j = 1; j <= n; j++) {
                fila[j] = Integer.valueOf(0);             // matriz llena de ceros
            }
            modelo.addRow(fila);
        }

        // Reemplazamos el modelo en la tabla existente para no duplicar componentes
        if (tabla == null) {
            tabla = new JTable(modelo);
        } else {
            tabla.setModel(modelo);
        }
        tabla.setRowHeight(26);
    }

    // ------------------------------------------------------------------
    // Conversion matriz -> grafo
    // ------------------------------------------------------------------

    /**
     * Lee la matriz escrita por el usuario, crea el objeto Grafo y avisa a la
     * ventana principal para que dibuje el grafo y calcule lista y recorridos.
     */
    private void convertir() {
        int n = modelo.getRowCount();
        Grafo g = new Grafo(n, chkDirigido.isSelected(), chkPonderado.isSelected());
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Object valor = modelo.getValueAt(i, j + 1);
                int numero = 0;
                if (valor instanceof Number) {
                    numero = ((Number) valor).intValue();
                } else if (valor != null) {
                    try {
                        numero = Integer.parseInt(valor.toString().trim());
                    } catch (NumberFormatException ex) {
                        numero = 0;                        // texto no valido = sin arista
                    }
                }
                if (numero < 0) {
                    numero = 0;                            // no se permiten pesos negativos
                }
                if (!g.isPonderado()) {
                    numero = (numero == 0) ? 0 : 1;        // sin pesos: 0 o 1
                }
                g.setValor(i, j, numero);
            }
        }
        g.colocarEnCirculo();                              // posiciones para el dibujo
        grafoActual = g;
        escuchador.grafoModificado(g);
    }

    /** Carga una matriz de ejemplo (5 vertices) para probar rapido el programa. */
    private void cargarEjemplo() {
        int[][] ejemplo = {
                {0, 1, 0, 1, 0},
                {1, 0, 1, 0, 1},
                {0, 1, 0, 0, 1},
                {1, 0, 0, 0, 1},
                {0, 1, 1, 1, 0}
        };
        spnN.setValue(Integer.valueOf(5));
        crearTabla(5);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                modelo.setValueAt(Integer.valueOf(ejemplo[i][j]), i, j + 1);
            }
        }
        convertir();
    }

    /** Devuelve el grafo actual (lo usa la ventana principal). */
    public Grafo getGrafoActual() {
        return grafoActual;
    }
}

