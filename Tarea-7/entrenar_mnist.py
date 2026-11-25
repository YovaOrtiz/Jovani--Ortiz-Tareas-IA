# Tarea 7 - parte 1: entrenar el modelo con MNIST y guardarlo
# al final se genera modelo_mnist.keras que usa app_digitos.py
import matplotlib.pyplot as plt
import numpy as np
import tensorflow as tf
from tensorflow.keras import layers, models

# cargo los datos de mnist
(x_train, y_train), (x_test, y_test) = tf.keras.datasets.mnist.load_data()
print("Entrenamiento:", x_train.shape, " Prueba:", x_test.shape)

# paso los pixeles a 0-1 y le agrego una dimension extra para el canal
x_train = (x_train / 255.0).astype("float32")[..., np.newaxis]
x_test = (x_test / 255.0).astype("float32")[..., np.newaxis]

# guardo una imagen con algunos ejemplos
plt.figure(figsize=(8, 2))
for i in range(8):
    plt.subplot(1, 8, i + 1)
    plt.imshow(x_train[i, :, :, 0], cmap="gray")
    plt.title(y_train[i])
    plt.axis("off")
plt.savefig("ejemplos_mnist.png")
plt.close()

# el modelo
modelo = models.Sequential([
    layers.Input(shape=(28, 28, 1)),

    # estas tres capas mueven y rotan un poco los numeros al entrenar,
    # asi reconoce mejor lo que dibujo con el mouse
    layers.RandomRotation(0.08),
    layers.RandomZoom(0.1),
    layers.RandomTranslation(0.1, 0.1),

    layers.Conv2D(32, (3, 3), activation="relu"),
    layers.MaxPooling2D((2, 2)),
    layers.Conv2D(64, (3, 3), activation="relu"),
    layers.MaxPooling2D((2, 2)),

    layers.Flatten(),
    layers.Dense(128, activation="relu"),
    layers.Dropout(0.3),
    layers.Dense(10, activation="softmax"),  # una salida por cada digito
])

modelo.compile(optimizer="adam",
               loss="sparse_categorical_crossentropy",
               metrics=["accuracy"])
modelo.summary()

# entrenar
historial = modelo.fit(
    x_train, y_train,
    epochs=8,
    batch_size=128,
    validation_split=0.1,
)

perdida, precision = modelo.evaluate(x_test, y_test, verbose=0)
print(f"\nPrecision en datos de prueba: {precision * 100:.2f}%")

# guardar el modelo
modelo.save("modelo_mnist.keras")
print("Modelo guardado en modelo_mnist.keras")

# graficas de como fue aprendiendo
fig, ax = plt.subplots(1, 2, figsize=(11, 4))
ax[0].plot(historial.history["accuracy"], label="entrenamiento")
ax[0].plot(historial.history["val_accuracy"], label="validacion")
ax[0].set_title("Precision por epoca")
ax[0].set_xlabel("Epoca")
ax[0].legend()
ax[1].plot(historial.history["loss"], label="entrenamiento")
ax[1].plot(historial.history["val_loss"], label="validacion")
ax[1].set_title("Perdida por epoca")
ax[1].set_xlabel("Epoca")
ax[1].legend()
plt.tight_layout()
plt.savefig("curvas_entrenamiento.png", dpi=120)
plt.show()
