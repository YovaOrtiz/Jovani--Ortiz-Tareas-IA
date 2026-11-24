import java.util.Scanner;

/*
 * Tarea 1 - Arbol Binario de Busqueda (ABB)
 * Repaso: insertar buscar eliminar y recorridos.
 * Regla del ABB: lo menor va a la izquierda y lo mayor a la derecha.
 */
public class ArbolBinario {

    // Cada nodo guarda un valor y sus dos hijos
    static class Nodo {
        int valor;
        Nodo izq;
        Nodo der;

        Nodo(int valor) {
            this.valor = valor;
        }
    }

    private Nodo raiz;

    public void insertar(int valor) {
        raiz = insertar(raiz, valor);
    }

    private Nodo insertar(Nodo nodo, int valor) {
        if (nodo == null) {
            return new Nodo(valor);
        }
        if (valor < nodo.valor) {
            nodo.izq = insertar(nodo.izq, valor);
        } else if (valor > nodo.valor) {
            nodo.der = insertar(nodo.der, valor);
        }
        // si es igual no se inserta "no se permiten repetidos"
        return nodo;
    }

    public boolean buscar(int valor) {
        Nodo aux = raiz;
        while (aux != null) {
            if (valor == aux.valor) {
                return true;
            }
            aux = (valor < aux.valor) ? aux.izq : aux.der;
        }
        return false;
    }

    public void eliminar(int valor) {
        raiz = eliminar(raiz, valor);
    }

    private Nodo eliminar(Nodo nodo, int valor) {
        if (nodo == null) {
            return null;
        }
        if (valor < nodo.valor) {
            nodo.izq = eliminar(nodo.izq, valor);
        } else if (valor > nodo.valor) {
            nodo.der = eliminar(nodo.der, valor);
        } else {
            // caso 1 y 2: sin hijos o con un solo hijo
            if (nodo.izq == null) return nodo.der;
            if (nodo.der == null) return nodo.izq;

            // caso 3: dos hijos se reemplaza por el menor del subarbol derecho
            Nodo sucesor = nodo.der;
            while (sucesor.izq != null) {
                sucesor = sucesor.izq;
            }
            nodo.valor = sucesor.valor;
            nodo.der = eliminar(nodo.der, sucesor.valor);
        }
        return nodo;
    }

    // Recorridos
    public void inorden(Nodo n) {
        if (n == null) return;
        inorden(n.izq);
        System.out.print(n.valor + " ");
        inorden(n.der);
    }

    public void preorden(Nodo n) {
        if (n == null) return;
        System.out.print(n.valor + " ");
        preorden(n.izq);
        preorden(n.der);
    }

    public void postorden(Nodo n) {
        if (n == null) return;
        postorden(n.izq);
        postorden(n.der);
        System.out.print(n.valor + " ");
    }

    public int altura(Nodo n) {
        if (n == null) return 0;
        return 1 + Math.max(altura(n.izq), altura(n.der));
    }

    // Dibuja el arbol de lado (la raiz queda a la izquierda)
    public void mostrar(Nodo n, int nivel) {
        if (n == null) return;
        mostrar(n.der, nivel + 1);
        System.out.println("    ".repeat(nivel) + n.valor);
        mostrar(n.izq, nivel + 1);
    }

    public static void main(String[] args) {
        ArbolBinario arbol = new ArbolBinario();
        Scanner sc = new Scanner(System.in);

        // valores de prueba
        int[] datos = {50, 30, 70, 20, 40, 60, 80, 35, 65};
        for (int d : datos) {
            arbol.insertar(d);
        }

        int op;
        do {
            System.out.println("\n===== ARBOL BINARIO DE BUSQUEDA =====");
            System.out.println("1. Insertar");
            System.out.println("2. Buscar");
            System.out.println("3. Eliminar");
            System.out.println("4. Recorridos");
            System.out.println("5. Mostrar arbol");
            System.out.println("0. Salir");
            System.out.print("Opcion: ");
            op = sc.nextInt();

            switch (op) {
                case 1:
                    System.out.print("Valor a insertar: ");
                    arbol.insertar(sc.nextInt());
                    break;
                case 2:
                    System.out.print("Valor a buscar: ");
                    int b = sc.nextInt();
                    System.out.println(arbol.buscar(b) ? "Si existe" : "No existe");
                    break;
                case 3:
                    System.out.print("Valor a eliminar: ");
                    int e = sc.nextInt();
                    if (arbol.buscar(e)) {
                        arbol.eliminar(e);
                        System.out.println("Eliminado");
                    } else {
                        System.out.println("Ese valor no esta en el arbol");
                    }
                    break;
                case 4:
                    System.out.print("Inorden:   "); arbol.inorden(arbol.raiz);   System.out.println();
                    System.out.print("Preorden:  "); arbol.preorden(arbol.raiz);  System.out.println();
                    System.out.print("Postorden: "); arbol.postorden(arbol.raiz); System.out.println();
                    break;
                case 5:
                    arbol.mostrar(arbol.raiz, 0);
                    System.out.println("Altura: " + arbol.altura(arbol.raiz));
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
