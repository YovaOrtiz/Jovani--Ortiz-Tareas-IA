# Tarea 7 - parte 2: app para probar el modelo
# dibujas un numero con el mouse y te dice cual es mientras dibujas
# primero hay que correr entrenar_mnist.py para que exista modelo_mnist.keras
import tkinter as tk

import numpy as np
import tensorflow as tf
from PIL import Image, ImageDraw

TAM = 280      # tamano del lienzo
GROSOR = 18    # grosor del trazo

# cargo el modelo que ya entrene
modelo = tf.keras.models.load_model("modelo_mnist.keras")


def preparar_imagen(img):
    # deja el dibujo como las imagenes de mnist: 28x28, fondo negro, trazo blanco
    arr = np.array(img)
    filas = np.where(arr.max(axis=1) > 0)[0]
    cols = np.where(arr.max(axis=0) > 0)[0]
    if len(filas) == 0:
        return None

    # recorto solo donde dibuje
    recorte = img.crop((cols[0], filas[0], cols[-1] + 1, filas[-1] + 1))

    # lo escalo para que el lado mas grande mida 20 px
    ancho, alto = recorte.size
    escala = 20.0 / max(ancho, alto)
    nuevo = (max(1, round(ancho * escala)), max(1, round(alto * escala)))
    recorte = recorte.resize(nuevo, Image.LANCZOS)

    # lo pego en medio de una imagen de 28x28
    final = Image.new("L", (28, 28), 0)
    final.paste(recorte, ((28 - nuevo[0]) // 2, (28 - nuevo[1]) // 2))

    # mnist centra los numeros por su centro de masa, aqui lo acomodo igual
    a = np.array(final, dtype="float32")
    ys, xs = np.nonzero(a)
    pesos = a[ys, xs]
    dy = int(round(14 - np.average(ys, weights=pesos)))
    dx = int(round(14 - np.average(xs, weights=pesos)))
    a = np.roll(a, (dy, dx), axis=(0, 1))

    return (a / 255.0).reshape(1, 28, 28, 1)


class App:
    def __init__(self, root):
        self.root = root
        root.title("Clasificador de digitos - MNIST")
        root.resizable(False, False)

        izq = tk.Frame(root, padx=10, pady=10)
        izq.pack(side="left")
        der = tk.Frame(root, padx=10, pady=10)
        der.pack(side="left", fill="y")

        tk.Label(izq, text="Dibuja un numero (0-9)", font=("Arial", 12)).pack()
        self.canvas = tk.Canvas(izq, width=TAM, height=TAM, bg="black", cursor="cross")
        self.canvas.pack(pady=5)
        tk.Button(izq, text="Borrar", width=12, command=self.borrar).pack()

        # esta imagen no se ve, es donde tambien dibujo para pasarsela al modelo
        self.imagen = Image.new("L", (TAM, TAM), 0)
        self.dibujo = ImageDraw.Draw(self.imagen)
        self.ultimo = None
        self.pendiente = False

        self.canvas.bind("<B1-Motion>", self.pintar)
        self.canvas.bind("<ButtonRelease-1>", self.soltar)
        # clic derecho borra (en mac es Button-2 y en windows Button-3)
        self.canvas.bind("<Button-2>", lambda e: self.borrar())
        self.canvas.bind("<Button-3>", lambda e: self.borrar())

        self.resultado = tk.Label(der, text="?", font=("Arial", 64, "bold"))
        self.resultado.pack()
        self.confianza = tk.Label(der, text="", font=("Arial", 11))
        self.confianza.pack(pady=(0, 10))

        # barras con la probabilidad de cada digito
        self.barras = tk.Canvas(der, width=220, height=230)
        self.barras.pack()
        self.dibujar_barras(np.zeros(10))

    def pintar(self, e):
        if self.ultimo:
            x0, y0 = self.ultimo
            self.canvas.create_line(x0, y0, e.x, e.y, fill="white", width=GROSOR,
                                    capstyle=tk.ROUND, smooth=True)
            self.dibujo.line([x0, y0, e.x, e.y], fill=255, width=GROSOR)
        r = GROSOR // 2
        self.dibujo.ellipse([e.x - r, e.y - r, e.x + r, e.y + r], fill=255)
        self.ultimo = (e.x, e.y)

        # predice mientras dibujo pero solo cada 150 ms para que no se trabe
        if not self.pendiente:
            self.pendiente = True
            self.root.after(150, self.predecir)

    def soltar(self, e):
        self.ultimo = None
        self.predecir()

    def predecir(self):
        self.pendiente = False
        entrada = preparar_imagen(self.imagen)
        if entrada is None:
            return
        probs = modelo(entrada, training=False).numpy()[0]
        digito = int(np.argmax(probs))
        self.resultado.config(text=str(digito))
        self.confianza.config(text=f"Confianza: {probs[digito] * 100:.1f}%")
        self.dibujar_barras(probs)

    def dibujar_barras(self, probs):
        self.barras.delete("all")
        for i, p in enumerate(probs):
            y = i * 23 + 5
            self.barras.create_text(10, y + 8, text=str(i), font=("Arial", 10))
            self.barras.create_rectangle(25, y, 25 + 140, y + 16, outline="#ccc")
            color = "#2e8b57" if p == probs.max() and p > 0 else "#7aa6d8"
            self.barras.create_rectangle(25, y, 25 + 140 * p, y + 16, fill=color, width=0)
            self.barras.create_text(215, y + 8, text=f"{p * 100:.0f}%", anchor="e", font=("Arial", 8))

    def borrar(self):
        self.canvas.delete("all")
        self.dibujo.rectangle([0, 0, TAM, TAM], fill=0)
        self.resultado.config(text="?")
        self.confianza.config(text="")
        self.dibujar_barras(np.zeros(10))


if __name__ == "__main__":
    root = tk.Tk()
    App(root)
    root.mainloop()
