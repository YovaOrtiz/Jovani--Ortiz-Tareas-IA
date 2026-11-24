import java.util.Scanner;

// Tarea 1 - Arbol binario de busqueda
// lo que va antes en el abecedario se va a la izquierda y lo que va despues a la derecha
public class Arbol {

    Nodo raiz;

    // dice si el arbol no tiene nada
    public boolean vacio() {
        return raiz == null;
    }

    public void insertar(String nombre) {
        raiz = insertar(raiz, nombre);
    }

    private Nodo insertar(Nodo nodo, String nombre) {
        if (nodo == null) {
            return new Nodo(nombre);
        }
        int c = nombre.compareToIgnoreCase(nodo.nombre);
        if (c < 0) {
            nodo.izq = insertar(nodo.izq, nombre);
        } else if (c > 0) {
            nodo.der = insertar(nodo.der, nombre);
        }
        // si es igual no lo meto para que no se repita
        return nodo;
    }

    // regresa el nodo si lo encuentra y si no regresa null
    public Nodo buscarNodo(String nombre) {
        Nodo aux = raiz;
        while (aux != null) {
            int c = nombre.compareToIgnoreCase(aux.nombre);
            if (c == 0) {
                return aux;
            }
            if (c < 0) {
                aux = aux.izq;
            } else {
                aux = aux.der;
            }
        }
        return null;
    }

    // recorrido inorden, sale en orden alfabetico
    public void inorden(Nodo n) {
        if (n == null) return;
        inorden(n.izq);
        System.out.print(n.nombre + " ");
        inorden(n.der);
    }

    public static void main(String[] args) {
        Arbol arbol = new Arbol();
        Scanner sc = new Scanner(System.in);

        System.out.println("Arbol vacio? " + arbol.vacio());

        // nombres de prueba
        String[] datos = {"Marco", "Ana", "Sofia", "Beto", "Luis", "Rosa", "Zoe"};
        for (String d : datos) {
            arbol.insertar(d);
        }
        System.out.println("Arbol vacio? " + arbol.vacio());

        int op;
        do {
            System.out.println("\n===== ARBOL BINARIO DE BUSQUEDA =====");
            System.out.println("1. Insertar");
            System.out.println("2. Buscar nodo");
            System.out.println("3. Mostrar en orden");
            System.out.println("4. Esta vacio?");
            System.out.println("0. Salir");
            System.out.print("Opcion: ");
            op = sc.nextInt();

            switch (op) {
                case 1:
                    System.out.print("Nombre a insertar: ");
                    arbol.insertar(sc.next());
                    break;
                case 2:
                    System.out.print("Nombre a buscar: ");
                    Nodo r = arbol.buscarNodo(sc.next());
                    if (r != null) {
                        System.out.println("Encontrado: " + r.nombre);
                    } else {
                        System.out.println("No existe");
                    }
                    break;
                case 3:
                    arbol.inorden(arbol.raiz);
                    System.out.println();
                    break;
                case 4:
                    System.out.println(arbol.vacio() ? "Si esta vacio" : "No esta vacio");
                    break;
                case 0:
                    System.out.println("Adios");
                    break;
                default:
                    System.out.println("Opcion no valida");
            }
        } while (op != 0);
    }
}
