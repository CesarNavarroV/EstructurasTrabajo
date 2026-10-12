package lab.estructuras;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * MODELO DEL GRAFO.
 *
 * Guarda el grafo de dos formas equivalentes:
 *  - matriz de adyacencia  -> int[][] (0 = no hay arista; otro valor = peso o 1)
 *  - posiciones de los vertices -> para poder dibujarlo en pantalla
 *
 * Permite convertir matriz -> grafo y grafo -> matriz, y ademas calcular
 * la lista de adyacencia y los recorridos en profundidad (DFS) y anchura (BFS).
 */
public class Grafo {

    // ------------------------------------------------------------------
    // Atributos
    // ------------------------------------------------------------------
    private int n;                       // cantidad de vertices del grafo
    private boolean dirigido;            // true = las aristas tienen direccion (flecha)
    private boolean ponderado;           // true = las aristas guardan un peso
    private int[][] matriz;              // matriz de adyacencia
    private List<NodoVisual> nodos;      // posicion (x, y) de cada vertice en el lienzo

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public Grafo(int n, boolean dirigido, boolean ponderado) {
        this.n = Math.max(n, 0);
        this.dirigido = dirigido;
        this.ponderado = ponderado;
        this.matriz = new int[this.n][this.n];
        this.nodos = new ArrayList<>();
        colocarEnCirculo();   // posiciones por defecto (circulo) para que se vea ordenado
    }

    // ------------------------------------------------------------------
    // Consultas basicas
    // ------------------------------------------------------------------
    public int getN() {
        return n;
    }

    public boolean isDirigido() {
        return dirigido;
    }

    public boolean isPonderado() {
        return ponderado;
    }

    public int[][] getMatriz() {
        return matriz;
    }

    public List<NodoVisual> getNodos() {
        return nodos;
    }

    /** Valor de la arista i -> j (0 significa "no hay arista"). */
    public int getValor(int i, int j) {
        if (i < 0 || j < 0 || i >= n || j >= n) {
            return 0;
        }
        return matriz[i][j];
    }

    /** Cambia el numero de vertices del grafo (la matriz se reinicia en 0). */
    public void setN(int nuevoN) {
        this.n = Math.max(nuevoN, 0);
        this.matriz = new int[this.n][this.n];
        this.nodos = new ArrayList<>();
        colocarEnCirculo();
    }

    /**
     * Cambia el tipo de grafo (dirigido / ponderado) y ajusta la matriz:
     *  - si deja de ser ponderado, los pesos se convierten en 1.
     *  - si deja de ser dirigido, la matriz se simetriza (se toma el valor mayor).
     */
    public void setTipo(boolean dirigido, boolean ponderado) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int v = matriz[i][j];
                if (!ponderado && v != 0) {
                    v = 1;                                // sin pesos: solo 0 o 1
                }
                matriz[i][j] = v;
            }
        }
        if (!dirigido) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    int v = Math.max(matriz[i][j], matriz[j][i]);
                    matriz[i][j] = v;                     // simetria
                }
            }
        }
        this.dirigido = dirigido;
        this.ponderado = ponderado;
    }

    // ------------------------------------------------------------------
    // Escritura de la matriz
    // ------------------------------------------------------------------

    /**
     * Escribe el valor de la arista i -> j.
     * Si el grafo NO es dirigido, se refleja el valor en j -> i (simetria).
     * Si el grafo NO es ponderado, cualquier valor distinto de 0 se guarda como 1.
     */
    public void setValor(int i, int j, int valor) {
        if (i < 0 || j < 0 || i >= n || j >= n) {
            return;
        }
        if (!ponderado && valor != 0) {
            valor = 1;                       // grafo simple: solo 0 o 1
        }
        matriz[i][j] = valor;
        if (!dirigido && i != j) {
            matriz[j][i] = valor;            // espejo para grafos no dirigidos
        }
    }

    /** Reemplaza toda la matriz por una nueva (copia defensiva). */
    public void setMatriz(int[][] nueva) {
        if (nueva == null) {
            return;
        }
        this.n = nueva.length;
        this.matriz = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matriz[i][j] = nueva[i][j];
            }
        }
        colocarEnCirculo();          // recalcula las posiciones para el dibujo
    }

    // ------------------------------------------------------------------
    // Operaciones usadas al DIBUJAR el grafo con el mouse
    // ------------------------------------------------------------------

    /** Agrega un vertice nuevo al final y guarda su posicion en el lienzo. */
    public void agregarVertice(int x, int y) {
        int[][] nueva = new int[n + 1][n + 1];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                nueva[i][j] = matriz[i][j];
            }
        }
        matriz = nueva;
        List<NodoVisual> copia = new ArrayList<>(nodos);
        nodos = copia;
        nodos.add(new NodoVisual(n, x, y));
        n = n + 1;
    }

    /**
     * Elimina el vertice indicado: quita su fila y columna de la matriz,
     * quita su circulo y RENUMERA los vertices siguientes para que los ids
     * sigan coincidiendo con las filas/columnas.
     */
    public void eliminarVertice(int id) {
        if (id < 0 || id >= n) {
            return;
        }
        int[][] nueva = new int[n - 1][n - 1];
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1; j++) {
                int fila = (i < id) ? i : i + 1;      // se salta la fila eliminada
                int col = (j < id) ? j : j + 1;       // se salta la columna eliminada
                nueva[i][j] = matriz[fila][col];
            }
        }
        matriz = nueva;
        List<NodoVisual> copia = new ArrayList<>(nodos);
        for (int i = 0; i < copia.size(); i++) {
            if (copia.get(i).getId() == id) {
                copia.remove(i);
                break;
            }
        }
        for (int i = 0; i < copia.size(); i++) {       // renumerar ids restantes
            copia.get(i).setId(i);
        }
        nodos = copia;
        n = n - 1;
    }

    /** Crea (o actualiza) la arista entre dos vertices. */
    public void agregarArista(int i, int j, int peso) {
        setValor(i, j, peso);
    }

    /** Elimina la arista entre dos vertices. */
    public void eliminarArista(int i, int j) {
        if (i < 0 || j < 0 || i >= n || j >= n) {
            return;
        }
        matriz[i][j] = 0;
        if (!dirigido) {
            matriz[j][i] = 0;
        }
    }
    /** Deja el grafo sin vertices. */
    public void limpiar() {
        n = 0;
        matriz = new int[0][0];
        nodos = new ArrayList<>();
    }

    /** true si la matriz esta toda en ceros. */
    public boolean esVacio() {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (matriz[i][j] != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Presentacion
    // ------------------------------------------------------------------

    /** Coloca los vertices sobre una circunferencia centrada en el lienzo. */
    public void colocarEnCirculo() {
        nodos = new ArrayList<>();
        if (n == 0) {
            return;
        }
        int centroX = 360;
        int centroY = 250;
        int radio = Math.max(120, Math.min(190, 40 + 18 * n));
        for (int i = 0; i < n; i++) {
            double angulo = 2 * Math.PI * i / n - Math.PI / 2;   // empieza arriba
            int x = (int) Math.round(centroX + radio * Math.cos(angulo));
            int y = (int) Math.round(centroY + radio * Math.sin(angulo));
            nodos.add(new NodoVisual(i, x, y));
        }
    }

    /** Texto de la matriz con encabezados V0, V1... listo para mostrar. */
    public String matrizComoTexto() {
        StringBuilder sb = new StringBuilder();
        sb.append("     ");
        for (int j = 0; j < n; j++) {
            sb.append(String.format("V%-4d", j));
        }
        sb.append('\n');
        for (int i = 0; i < n; i++) {
            sb.append(String.format("V%-3d ", i));
            for (int j = 0; j < n; j++) {
                sb.append(String.format("%-5d", matriz[i][j]));
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    /** Texto de la LISTA DE ADYACENCIA: para cada vertice, sus vecinos. */
    public String listaAdyacenciaComoTexto() {
        if (n == 0) {
            return "(grafo vacio)\n";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append("V").append(i).append(" -> ");
            boolean primero = true;
            for (int j = 0; j < n; j++) {
                if (matriz[i][j] != 0) {
                    if (!primero) {
                        sb.append(" , ");
                    }
                    sb.append("V").append(j);
                    if (ponderado) {
                        sb.append("(").append(matriz[i][j]).append(")");
                    }
                    primero = false;
                }
            }
            if (primero) {
                sb.append("(sin vecinos)");
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    /** Formatea un recorrido como V0 -> V3 -> V1. */
    public static String recorridoComoTexto(List<Integer> r) {
        if (r.isEmpty()) {
            return "(vacio)";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < r.size(); i++) {
            sb.append("V").append(r.get(i));
            if (i < r.size() - 1) {
                sb.append(" -> ");
            }
        }
        return sb.toString();
    }


    // ------------------------------------------------------------------
    // RECORRIDOS
    // ------------------------------------------------------------------

    /**
     * RECORRIDO EN PROFUNDIDAD (DFS): baja lo mas que pueda por una rama
     * antes de retroceder. Implementacion recursiva con marca de visitados.
     */
    public List<Integer> recorridoProfundidad(int inicio) {
        List<Integer> resultado = new ArrayList<>();
        if (inicio < 0 || inicio >= n) {
            return resultado;
        }
        boolean[] visitado = new boolean[n];
        dfs(inicio, visitado, resultado);
        return resultado;
    }

    private void dfs(int actual, boolean[] visitado, List<Integer> resultado) {
        visitado[actual] = true;
        resultado.add(actual);
        for (int j = 0; j < n; j++) {
            if (matriz[actual][j] != 0 && !visitado[j]) {
                dfs(j, visitado, resultado);
            }
        }
    }

    /**
     * RECORRIDO EN ANCHURA (BFS): visita todos los vecinos de un vertice antes
     * de pasar al siguiente nivel. Implementacion con una cola (Queue).
     */
    public List<Integer> recorridoAnchura(int inicio) {
        List<Integer> resultado = new ArrayList<>();
        if (inicio < 0 || inicio >= n) {
            return resultado;
        }
        boolean[] visitado = new boolean[n];
        Queue<Integer> cola = new LinkedList<>();
        visitado[inicio] = true;
        cola.add(inicio);
        while (!cola.isEmpty()) {
            int actual = cola.poll();
            resultado.add(actual);
            for (int j = 0; j < n; j++) {
                if (matriz[actual][j] != 0 && !visitado[j]) {
                    visitado[j] = true;
                    cola.add(j);
                }
            }
        }
        return resultado;
    }
}

