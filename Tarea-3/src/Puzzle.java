import java.util.*;

/*
 * Representacion del 8-puzzle.
 * El tablero se guarda como un String de 9 caracteres, leido por filas.
 * El 0 es el espacio vacio. Ejemplo:
 *
 *   "123456780"  ->   1 2 3
 *                     4 5 6
 *                     7 8 _
 */
public class Puzzle {

    public static final String META = "123456780";

    // movimientos del espacio vacio: {cambio de fila, cambio de columna, nombre}
    private static final int[][] MOV = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    private static final String[] NOMBRE = {"Arriba", "Abajo", "Izquierda", "Derecha"};

    // Un sucesor: el nuevo tablero y el movimiento que lo genero
    public static class Sucesor {
        String estado;
        String accion;

        Sucesor(String estado, String accion) {
            this.estado = estado;
            this.accion = accion;
        }
    }

    public static List<Sucesor> sucesores(String estado) {
        List<Sucesor> lista = new ArrayList<>(4);
        int pos = estado.indexOf('0');
        int fila = pos / 3, col = pos % 3;

        for (int i = 0; i < 4; i++) {
            int nf = fila + MOV[i][0];
            int nc = col + MOV[i][1];
            if (nf < 0 || nf > 2 || nc < 0 || nc > 2) continue; // se sale del tablero

            char[] t = estado.toCharArray();
            int nuevaPos = nf * 3 + nc;
            t[pos] = t[nuevaPos];
            t[nuevaPos] = '0';
            lista.add(new Sucesor(new String(t), NOMBRE[i]));
        }
        return lista;
    }

    // Un 8-puzzle solo tiene solucion si el numero de inversiones es par
    public static boolean tieneSolucion(String estado) {
        int inversiones = 0;
        String s = estado.replace("0", "");
        for (int i = 0; i < s.length(); i++) {
            for (int j = i + 1; j < s.length(); j++) {
                if (s.charAt(i) > s.charAt(j)) inversiones++;
            }
        }
        return inversiones % 2 == 0;
    }

    public static boolean esValido(String estado) {
        if (estado.length() != 9) return false;
        for (char c = '0'; c <= '8'; c++) {
            if (estado.indexOf(c) == -1) return false;
        }
        return true;
    }

    // Revuelve el tablero meta haciendo movimientos al azar (siempre queda resoluble)
    public static String revolver(int movimientos, long semilla) {
        Random rnd = new Random(semilla);
        String actual = META;
        String anterior = null;
        for (int i = 0; i < movimientos; i++) {
            List<Sucesor> suc = sucesores(actual);
            Sucesor s;
            do {
                s = suc.get(rnd.nextInt(suc.size()));
            } while (s.estado.equals(anterior)); // no deshacer el movimiento anterior
            anterior = actual;
            actual = s.estado;
        }
        return actual;
    }

    public static void imprimir(String estado) {
        for (int i = 0; i < 9; i += 3) {
            System.out.println("   " + celda(estado.charAt(i)) + " " + celda(estado.charAt(i + 1))
                    + " " + celda(estado.charAt(i + 2)));
        }
    }

    private static String celda(char c) {
        return c == '0' ? "_" : String.valueOf(c);
    }
}
