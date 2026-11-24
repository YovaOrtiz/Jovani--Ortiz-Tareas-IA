import java.util.*;

/*
 * Algoritmos de busqueda no informada aplicados al 8-puzzle:
 *  1. Primero en anchura (BFS)
 *  2. Costo uniforme (UCS)
 *  3. Primero en profundidad (DFS)
 *  4. Profundidad limitada (DLS)
 *  5. Profundidad iterativa (IDDFS) -> usa DLS con limite 0, 1, 2, ...
 *  6. Bidireccional (opcional)
 *
 * Cada movimiento cuesta 1.
 */
public class Busquedas {

    // Nodo del arbol de busqueda
    static class Nodo {
        String estado;
        Nodo padre;
        String accion;
        int costo;    // g(n): costo acumulado desde el inicio
        long orden;   // para desempatar en la cola de prioridad (el que entro primero sale primero)

        Nodo(String estado, Nodo padre, String accion, int costo) {
            this.estado = estado;
            this.padre = padre;
            this.accion = accion;
            this.costo = costo;
        }
    }

    // Lo que regresa cada algoritmo
    public static class Resultado {
        String nombre;
        boolean encontrado;
        List<String> acciones = new ArrayList<>();
        List<String> estados = new ArrayList<>();
        long expandidos;     // nodos que se sacaron de la frontera y se revisaron
        long fronteraMax;    // tamano maximo de la frontera (memoria)
        double tiempoMs;
        String nota = "";

        int pasos() {
            return acciones.size();
        }
    }

    // Arma la lista de movimientos siguiendo los padres desde la meta hasta el inicio
    private static void armarSolucion(Nodo meta, Resultado r) {
        LinkedList<String> acc = new LinkedList<>();
        LinkedList<String> est = new LinkedList<>();
        for (Nodo n = meta; n != null; n = n.padre) {
            est.addFirst(n.estado);
            if (n.accion != null) acc.addFirst(n.accion);
        }
        r.acciones = acc;
        r.estados = est;
        r.encontrado = true;
    }

    // ===================== 1. PRIMERO EN ANCHURA =====================
    // Frontera FIFO. La prueba de meta se hace al GENERAR el nodo (como en el libro).
    public static Resultado anchura(String inicio) {
        Resultado r = new Resultado();
        r.nombre = "Primero en anchura";
        long t0 = System.nanoTime();

        Nodo raiz = new Nodo(inicio, null, null, 0);
        if (inicio.equals(Puzzle.META)) {
            armarSolucion(raiz, r);
        } else {
            ArrayDeque<Nodo> frontera = new ArrayDeque<>();
            Set<String> alcanzados = new HashSet<>();
            frontera.add(raiz);
            alcanzados.add(inicio);

            buscar:
            while (!frontera.isEmpty()) {
                r.fronteraMax = Math.max(r.fronteraMax, frontera.size());
                Nodo n = frontera.poll();
                r.expandidos++;
                for (Puzzle.Sucesor s : Puzzle.sucesores(n.estado)) {
                    if (alcanzados.contains(s.estado)) continue;
                    Nodo hijo = new Nodo(s.estado, n, s.accion, n.costo + 1);
                    if (s.estado.equals(Puzzle.META)) {
                        armarSolucion(hijo, r);
                        break buscar;
                    }
                    alcanzados.add(s.estado);
                    frontera.add(hijo);
                }
            }
        }
        r.tiempoMs = (System.nanoTime() - t0) / 1e6;
        return r;
    }

    // ===================== 2. COSTO UNIFORME =====================
    // Frontera ordenada por g(n). La prueba de meta se hace al EXPANDIR,
    // asi se garantiza que la solucion encontrada es la de menor costo.
    public static Resultado costoUniforme(String inicio) {
        Resultado r = new Resultado();
        r.nombre = "Costo uniforme";
        long t0 = System.nanoTime();

        PriorityQueue<Nodo> frontera = new PriorityQueue<>((a, b) ->
                a.costo != b.costo ? Integer.compare(a.costo, b.costo) : Long.compare(a.orden, b.orden));
        Map<String, Integer> mejorCosto = new HashMap<>();
        Set<String> expandidos = new HashSet<>();
        long contador = 0;

        Nodo raiz = new Nodo(inicio, null, null, 0);
        raiz.orden = contador++;
        frontera.add(raiz);
        mejorCosto.put(inicio, 0);

        while (!frontera.isEmpty()) {
            r.fronteraMax = Math.max(r.fronteraMax, frontera.size());
            Nodo n = frontera.poll();
            if (expandidos.contains(n.estado)) continue; // copia vieja con mayor costo
            expandidos.add(n.estado);
            r.expandidos++;

            if (n.estado.equals(Puzzle.META)) {
                armarSolucion(n, r);
                break;
            }
            for (Puzzle.Sucesor s : Puzzle.sucesores(n.estado)) {
                int g = n.costo + 1;
                if (g < mejorCosto.getOrDefault(s.estado, Integer.MAX_VALUE)) {
                    mejorCosto.put(s.estado, g);
                    Nodo hijo = new Nodo(s.estado, n, s.accion, g);
                    hijo.orden = contador++;
                    frontera.add(hijo);
                }
            }
        }
        r.tiempoMs = (System.nanoTime() - t0) / 1e6;
        return r;
    }

    // ===================== 3. PRIMERO EN PROFUNDIDAD =====================
    // Frontera LIFO (pila). Se guarda un conjunto de visitados para no repetir estados,
    // si no, se quedaria dando vueltas para siempre.
    public static Resultado profundidad(String inicio) {
        Resultado r = new Resultado();
        r.nombre = "Primero en profundidad";
        long t0 = System.nanoTime();

        ArrayDeque<Nodo> pila = new ArrayDeque<>();
        Set<String> visitados = new HashSet<>();
        pila.push(new Nodo(inicio, null, null, 0));

        while (!pila.isEmpty()) {
            r.fronteraMax = Math.max(r.fronteraMax, pila.size());
            Nodo n = pila.pop();
            if (visitados.contains(n.estado)) continue;
            visitados.add(n.estado);
            r.expandidos++;

            if (n.estado.equals(Puzzle.META)) {
                armarSolucion(n, r);
                break;
            }
            for (Puzzle.Sucesor s : Puzzle.sucesores(n.estado)) {
                if (!visitados.contains(s.estado)) {
                    pila.push(new Nodo(s.estado, n, s.accion, n.costo + 1));
                }
            }
        }
        r.tiempoMs = (System.nanoTime() - t0) / 1e6;
        return r;
    }

    // ===================== 4. PROFUNDIDAD LIMITADA =====================
    // DFS recursivo que no baja mas alla del limite l.
    // Solo se evitan ciclos dentro del camino actual (no hay conjunto global de visitados),
    // por eso usa muy poca memoria: O(b*l).
    public static Resultado profundidadLimitada(String inicio, int limite) {
        Resultado r = new Resultado();
        r.nombre = "Profundidad limitada (l=" + limite + ")";
        long t0 = System.nanoTime();

        Set<String> enCamino = new HashSet<>();
        enCamino.add(inicio);
        Nodo meta = dlsRecursivo(new Nodo(inicio, null, null, 0), limite, enCamino, r);
        if (meta != null) {
            armarSolucion(meta, r);
        } else {
            r.nota = "no hay solucion con " + limite + " movimientos o menos";
        }
        r.tiempoMs = (System.nanoTime() - t0) / 1e6;
        return r;
    }

    private static Nodo dlsRecursivo(Nodo n, int limite, Set<String> enCamino, Resultado r) {
        r.expandidos++;
        r.fronteraMax = Math.max(r.fronteraMax, enCamino.size()); // profundidad de la recursion

        if (n.estado.equals(Puzzle.META)) return n;
        if (limite == 0) return null; // se corta aqui

        for (Puzzle.Sucesor s : Puzzle.sucesores(n.estado)) {
            if (enCamino.contains(s.estado)) continue; // evitar ciclos en el camino actual
            enCamino.add(s.estado);
            Nodo encontrado = dlsRecursivo(new Nodo(s.estado, n, s.accion, n.costo + 1),
                    limite - 1, enCamino, r);
            enCamino.remove(s.estado);
            if (encontrado != null) return encontrado;
        }
        return null;
    }

    // ===================== 5. PROFUNDIDAD ITERATIVA =====================
    // Combina profundidad limitada con un limite que va creciendo: 0, 1, 2, 3...
    // Encuentra la solucion mas corta (como BFS) usando poca memoria (como DFS).
    public static Resultado profundidadIterativa(String inicio, int limiteMaximo) {
        Resultado r = new Resultado();
        r.nombre = "Profundidad iterativa";
        long t0 = System.nanoTime();

        for (int l = 0; l <= limiteMaximo; l++) {
            Resultado parcial = profundidadLimitada(inicio, l);
            r.expandidos += parcial.expandidos; // se suman los nodos de todas las vueltas
            r.fronteraMax = Math.max(r.fronteraMax, parcial.fronteraMax);
            if (parcial.encontrado) {
                r.encontrado = true;
                r.acciones = parcial.acciones;
                r.estados = parcial.estados;
                r.nota = "encontrada con limite " + l;
                break;
            }
        }
        if (!r.encontrado) r.nota = "no se encontro hasta el limite " + limiteMaximo;
        r.tiempoMs = (System.nanoTime() - t0) / 1e6;
        return r;
    }

    // ===================== 6. BIDIRECCIONAL (opcional) =====================
    // Dos busquedas en anchura: una desde el inicio y otra desde la meta.
    // En el 8-puzzle todos los movimientos se pueden deshacer, por eso se puede buscar hacia atras.
    public static Resultado bidireccional(String inicio) {
        Resultado r = new Resultado();
        r.nombre = "Bidireccional";
        long t0 = System.nanoTime();

        if (inicio.equals(Puzzle.META)) {
            armarSolucion(new Nodo(inicio, null, null, 0), r);
            r.tiempoMs = (System.nanoTime() - t0) / 1e6;
            return r;
        }

        Map<String, Nodo> ladoInicio = new HashMap<>();
        Map<String, Nodo> ladoMeta = new HashMap<>();
        ArrayDeque<Nodo> colaInicio = new ArrayDeque<>();
        ArrayDeque<Nodo> colaMeta = new ArrayDeque<>();

        Nodo a = new Nodo(inicio, null, null, 0);
        Nodo b = new Nodo(Puzzle.META, null, null, 0);
        ladoInicio.put(inicio, a);
        ladoMeta.put(Puzzle.META, b);
        colaInicio.add(a);
        colaMeta.add(b);

        String cruce = null;
        while (!colaInicio.isEmpty() && !colaMeta.isEmpty() && cruce == null) {
            r.fronteraMax = Math.max(r.fronteraMax, colaInicio.size() + colaMeta.size());
            // se expande primero el lado que tenga la frontera mas chica
            if (colaInicio.size() <= colaMeta.size()) {
                cruce = expandirNivel(colaInicio, ladoInicio, ladoMeta, r);
            } else {
                cruce = expandirNivel(colaMeta, ladoMeta, ladoInicio, r);
            }
        }

        if (cruce != null) {
            // primera mitad: inicio -> cruce
            LinkedList<String> acc = new LinkedList<>();
            LinkedList<String> est = new LinkedList<>();
            for (Nodo n = ladoInicio.get(cruce); n != null; n = n.padre) {
                est.addFirst(n.estado);
                if (n.accion != null) acc.addFirst(n.accion);
            }
            // segunda mitad: cruce -> meta (los movimientos se invierten porque se buscaron al reves)
            for (Nodo n = ladoMeta.get(cruce); n.padre != null; n = n.padre) {
                est.addLast(n.padre.estado);
                acc.addLast(invertir(n.accion));
            }
            r.acciones = acc;
            r.estados = est;
            r.encontrado = true;
        }
        r.tiempoMs = (System.nanoTime() - t0) / 1e6;
        return r;
    }

    private static String expandirNivel(ArrayDeque<Nodo> cola, Map<String, Nodo> esteLado,
                                        Map<String, Nodo> otroLado, Resultado r) {
        int tam = cola.size();
        for (int i = 0; i < tam; i++) {
            Nodo n = cola.poll();
            r.expandidos++;
            for (Puzzle.Sucesor s : Puzzle.sucesores(n.estado)) {
                if (esteLado.containsKey(s.estado)) continue;
                Nodo hijo = new Nodo(s.estado, n, s.accion, n.costo + 1);
                esteLado.put(s.estado, hijo);
                if (otroLado.containsKey(s.estado)) return s.estado; // se encontraron
                cola.add(hijo);
            }
        }
        return null;
    }

    private static String invertir(String accion) {
        switch (accion) {
            case "Arriba": return "Abajo";
            case "Abajo": return "Arriba";
            case "Izquierda": return "Derecha";
            default: return "Izquierda";
        }
    }
}
