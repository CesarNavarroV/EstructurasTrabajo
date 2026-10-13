package lab.estructuras;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * PRUEBAS de la logica del grafo (matriz, lista de adyacencia, DFS y BFS).
 * No usan Swing: solo verifican que los calculos sean correctos.
 */
public class GrafoTest {

    /** Grafo no dirigido de 5 vertices con forma de "pentagono + diagonales". */
    private Grafo grafoEjemplo() {
        int[][] m = {
                {0, 1, 0, 1, 0},
                {1, 0, 1, 0, 1},
                {0, 1, 0, 0, 1},
                {1, 0, 0, 0, 1},
                {0, 1, 1, 1, 0}
        };
        Grafo g = new Grafo(5, false, false);
        g.setMatriz(m);
        return g;
    }

    @Test
    public void matrizSeCopiaYConservaTamaño() {
        Grafo g = grafoEjemplo();
        Assertions.assertEquals(5, g.getN());
        Assertions.assertEquals(1, g.getValor(0, 3));
        Assertions.assertEquals(0, g.getValor(0, 4));
    }

    @Test
    public void setTipoConviertePesosEnUnos() {
        int[][] m = {{0, 4, 0}, {0, 0, 2}, {0, 0, 0}};
        Grafo g = new Grafo(3, true, true);
        g.setMatriz(m);
        g.setTipo(true, false);                 // deja de ser ponderado
        Assertions.assertEquals(1, g.getValor(0, 1));
        Assertions.assertEquals(1, g.getValor(1, 2));
    }

    @Test
    public void setTipoSimetrizaCuandoNoEsDirigido() {
        int[][] m = {{0, 7, 0}, {0, 0, 0}, {0, 0, 0}};
        Grafo g = new Grafo(3, true, true);
        g.setMatriz(m);
        g.setTipo(false, true);                 // pasa a no dirigido
        Assertions.assertEquals(7, g.getValor(1, 0));   // se refleja la arista
        Assertions.assertEquals(7, g.getValor(0, 1));
    }

    @Test
    public void recorridoProfundidadDesdeCero() {
        Grafo g = grafoEjemplo();
        List<Integer> dfs = g.recorridoProfundidad(0);
        // Debe visitar los 5 vertices sin repetir ninguno
        Assertions.assertEquals(5, dfs.size());
        Assertions.assertEquals(Integer.valueOf(0), dfs.get(0));
    }

    @Test
    public void recorridoAnchuraDesdeCero() {
        Grafo g = grafoEjemplo();
        List<Integer> bfs = g.recorridoAnchura(0);
        // BFS empieza por el origen y luego sus vecinos V1 y V3
        Assertions.assertEquals(5, bfs.size());
        Assertions.assertEquals(Integer.valueOf(0), bfs.get(0));
        Assertions.assertEquals(Integer.valueOf(1), bfs.get(1));
        Assertions.assertEquals(Integer.valueOf(3), bfs.get(2));
    }

    @Test
    public void grafoDirigidoPonderado() {
        int[][] m = {
                {0, 5, 0},
                {0, 0, 2},
                {3, 0, 0}
        };
        Grafo g = new Grafo(3, true, true);
        g.setMatriz(m);

        List<Integer> dfs = g.recorridoProfundidad(0);
        Assertions.assertEquals(3, dfs.size());
        Assertions.assertEquals("V0 -> V1 -> V2", Grafo.recorridoComoTexto(dfs));

        // En dirigido: V0 no puede ir a V2 directamente (solo hay V2 -> V0)
        Assertions.assertEquals(0, g.getValor(0, 2));
        Assertions.assertEquals(3, g.getValor(2, 0));
    }

    @Test
    public void dibujarAgregaYEliminaVertices() {
        Grafo g = new Grafo(0, false, false);
        g.agregarVertice(10, 10);               // V0
        g.agregarVertice(50, 60);               // V1
        g.agregarArista(0, 1, 1);               // une V0-V1
        Assertions.assertEquals(2, g.getN());
        Assertions.assertEquals(1, g.getValor(0, 1));
        Assertions.assertEquals(1, g.getValor(1, 0));   // no dirigido: es simetrico

        g.eliminarVertice(0);                   // borra V0 y renumera
        Assertions.assertEquals(1, g.getN());
        Assertions.assertEquals(0, g.getValor(0, 0));   // queda un unico vertice aislado
    }

    @Test
    public void eliminarAristaRespetandoDireccion() {
        int[][] m = {
                {0, 1, 0},
                {0, 0, 1},
                {0, 0, 0}
        };
        Grafo g = new Grafo(3, true, false);
        g.setMatriz(m);
        g.eliminarArista(0, 1);
        Assertions.assertEquals(0, g.getValor(0, 1));
        Assertions.assertEquals(1, g.getValor(1, 2));   // no debe tocar otras aristas
    }

    @Test
    public void listaDeAdyacenciaComoTexto() {
        Grafo g = grafoEjemplo();
        String lista = g.listaAdyacenciaComoTexto();
        // V0 esta unido a V1 y V3
        Assertions.assertTrue(lista.contains("V0 -> V1 , V3"));
    }

    @Test
    public void matrizComoTextoContieneEncabezados() {
        Grafo g = grafoEjemplo();
        String texto = g.matrizComoTexto();
        Assertions.assertTrue(texto.contains("V0"));
        Assertions.assertTrue(texto.contains("V4"));
    }
}
