import java.util.*;

/*
 * Tarea 3 - Busquedas no informadas
 * Resolver el 8-puzzle con BFS, Costo uniforme, DFS, Profundidad limitada,
 * Profundidad iterativa y Bidireccional, y comparar complejidad y tiempo.
 */
public class Main {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("========== 8-PUZZLE: BUSQUEDAS NO INFORMADAS ==========");
        System.out.println("Meta:");
        Puzzle.imprimir(Puzzle.META);

        String inicio = elegirInicio();
        System.out.println("\nEstado inicial:");
        Puzzle.imprimir(inicio);

        if (!Puzzle.tieneSolucion(inicio)) {
            System.out.println("\nEse tablero NO tiene solucion (numero de inversiones impar).");
            return;
        }

        int limite = leerEntero("\nLimite para profundidad limitada (0 a 24, ej. 20): ", 0, 24);

        System.out.println("\nResolviendo...");
        List<Busquedas.Resultado> res = new ArrayList<>();
        res.add(Busquedas.anchura(inicio));
        res.add(Busquedas.costoUniforme(inicio));
        res.add(Busquedas.profundidad(inicio));
        res.add(Busquedas.profundidadLimitada(inicio, limite));
        res.add(Busquedas.profundidadIterativa(inicio, 31));
        res.add(Busquedas.bidireccional(inicio));

        // ---------- soluciones ----------
        for (Busquedas.Resultado r : res) {
            System.out.println("\n>> " + r.nombre);
            if (!r.encontrado) {
                System.out.println("   Sin solucion: " + r.nota);
                continue;
            }
            System.out.println("   Movimientos: " + r.pasos() + (r.nota.isEmpty() ? "" : "  (" + r.nota + ")"));
            if (r.pasos() <= 30) {
                System.out.println("   " + String.join(", ", r.acciones));
            } else {
                System.out.println("   (son demasiados para mostrarlos; DFS no busca el camino mas corto)");
            }
        }

        // ---------- tabla 1: resultados medidos ----------
        System.out.println("\nTABLA 1. RESULTADOS MEDIDOS");
        String linea = "-".repeat(92);
        System.out.println(linea);
        System.out.printf("%-34s %12s %16s %14s %12s%n",
                "Algoritmo", "Movimientos", "Nodos expandidos", "Frontera max", "Tiempo (ms)");
        System.out.println(linea);
        for (Busquedas.Resultado r : res) {
            System.out.printf("%-34s %12s %16d %14d %12.2f%n",
                    r.nombre,
                    r.encontrado ? String.valueOf(r.pasos()) : "-",
                    r.expandidos, r.fronteraMax, r.tiempoMs);
        }
        System.out.println(linea);

        // ---------- tabla 2: complejidad teorica ----------
        System.out.println("\nTABLA 2. COMPLEJIDAD TEORICA");
        String linea2 = "-".repeat(86);
        System.out.println(linea2);
        System.out.printf("%-24s %-12s %-18s %-18s %-10s%n", "Algoritmo", "Completa", "Tiempo", "Espacio", "Optima");
        System.out.println(linea2);
        String[][] teoria = {
                {"Primero en anchura", "Si", "O(b^(d+1))", "O(b^(d+1))", "Si*"},
                {"Costo uniforme", "Si", "O(b^(1+C*/e))", "O(b^(1+C*/e))", "Si"},
                {"Primero en profundidad", "No", "O(b^m)", "O(b*m)", "No"},
                {"Profundidad limitada", "No", "O(b^l)", "O(b*l)", "No"},
                {"Profundidad iterativa", "Si", "O(b^d)", "O(b*d)", "Si*"},
                {"Bidireccional", "Si", "O(b^(d/2))", "O(b^(d/2))", "Si*"},
        };
        for (String[] f : teoria) {
            System.out.printf("%-24s %-12s %-18s %-18s %-10s%n", f[0], f[1], f[2], f[3], f[4]);
        }
        System.out.println(linea2);
        System.out.println("b = factor de ramificacion (en el 8-puzzle entre 2 y 4, en promedio ~2.7)");
        System.out.println("d = profundidad de la solucion mas corta, m = profundidad maxima, l = limite");
        System.out.println("C* = costo de la solucion optima, e = costo minimo de un movimiento (aqui 1)");
        System.out.println("* optima cuando todos los movimientos cuestan lo mismo, como en el 8-puzzle");

        // ---------- ver la solucion paso a paso ----------
        System.out.print("\nVer la solucion de BFS paso a paso? (s/n): ");
        if (sc.next().equalsIgnoreCase("s")) {
            Busquedas.Resultado bfs = res.get(0);
            for (int i = 0; i < bfs.estados.size(); i++) {
                System.out.println(i == 0 ? "\nInicio:" : "\nPaso " + i + ": " + bfs.acciones.get(i - 1));
                Puzzle.imprimir(bfs.estados.get(i));
            }
        }
    }

    private static String elegirInicio() {
        System.out.println("\nElige el estado inicial:");
        System.out.println("1. Ejemplo facil   (4 movimientos)");
        System.out.println("2. Ejemplo medio   (12 movimientos)");
        System.out.println("3. Ejemplo dificil (20 movimientos)");
        System.out.println("4. Escribirlo yo");
        System.out.println("5. Revolver al azar");
        int op = leerEntero("Opcion: ", 1, 5);

        switch (op) {
            case 1: return "123506478";
            case 2: return "236408175";
            case 3: return "842106375";
            case 4:
                while (true) {
                    System.out.print("Escribe los 9 numeros por filas, 0 = vacio (ej. 123456078): ");
                    String s = sc.next().trim();
                    if (Puzzle.esValido(s)) return s;
                    System.out.println("Debe tener los numeros del 0 al 8 sin repetir.");
                }
            default:
                int mov = leerEntero("Cuantos movimientos al azar? (1 a 30): ", 1, 30);
                return Puzzle.revolver(mov, System.currentTimeMillis());
        }
    }

    private static int leerEntero(String msg, int min, int max) {
        while (true) {
            System.out.print(msg);
            if (sc.hasNextInt()) {
                int n = sc.nextInt();
                if (n >= min && n <= max) return n;
            } else {
                sc.next();
            }
            System.out.println("Escribe un numero entre " + min + " y " + max + ".");
        }
    }
}
