# Tarea 5 - Perceptron con Iris
import numpy as np
import matplotlib.pyplot as plt
from sklearn.datasets import load_iris
from sklearn.linear_model import Perceptron
from sklearn.model_selection import train_test_split

iris = load_iris()

# me quedo con setosa y versicolor nada mas, y uso largo y ancho del petalo
X = iris.data[iris.target < 2][:, [2, 3]]
y = iris.target[iris.target < 2]

# separo en entrenamiento y prueba
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=3)

# entreno el perceptron
p = Perceptron(max_iter=100, eta0=0.1)
p.fit(X_train, y_train)

print("pesos:", p.coef_[0])
print("bias:", p.intercept_[0])
print("precision:", p.score(X_test, y_test) * 100, "%")

# una flor nueva para ver que dice
flor = [[4.5, 1.4]]
r = p.predict(flor)[0]
print("la flor nueva es", iris.target_names[r])

# grafica, la linea sale de despejar y en w1*x + w2*y + b = 0
w1, w2 = p.coef_[0]
b = p.intercept_[0]
x = np.linspace(0.5, 5.5, 50)
y_l = -(w1 * x + b) / w2

plt.scatter(X[y == 0, 0], X[y == 0, 1], color="purple", label="Setosa")
plt.scatter(X[y == 1, 0], X[y == 1, 1], color="orange", label="Versicolor")
plt.scatter(4.5, 1.4, color="black", marker="*", s=200, label="Flor nueva")
plt.plot(x, y_l, "k--", label="Frontera")
plt.xlabel("Largo del petalo")
plt.ylabel("Ancho del petalo")
plt.title("Perceptron Setosa vs Versicolor")
plt.ylim(-0.2, 2.2)
plt.legend()
plt.savefig("perceptron_iris.png")
plt.show()
