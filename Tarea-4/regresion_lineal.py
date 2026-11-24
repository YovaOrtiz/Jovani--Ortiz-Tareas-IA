"""
Tarea 4 - Regresion Lineal con el dataset California Housing
"""
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score
from sklearn.model_selection import train_test_split

# dataset California Housing (viene incluido en sklearn)
from sklearn.datasets import fetch_california_housing
df = fetch_california_housing(as_frame=True).frame
df = df.rename(columns={"MedInc": "ingreso", "MedHouseVal": "precio"})

print("Tamano:", df.shape)
print(df.head())

# que tanto se relaciona cada variable con el precio
print("\nCorrelacion con el precio:")
print(df.corr()["precio"].drop("precio").sort_values(ascending=False).round(3))

y = df["precio"]


def mostrar_metricas(nombre, real, pred):
    mse = mean_squared_error(real, pred)
    r2 = r2_score(real, pred)
    print("\n---", nombre, "---")
    print("MAE :", round(mean_absolute_error(real, pred), 4))
    print("MSE :", round(mse, 4))
    print("RMSE:", round(np.sqrt(mse), 4), "(como $", round(np.sqrt(mse) * 100000), "de error)")
    print("R2  :", round(r2, 4))
    return r2


# ---------- Parte 1: regresion simple ----------
# uso el ingreso porque es el que mas correlacion tiene con el precio
X = df[["ingreso"]]
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=7)

modelo1 = LinearRegression()
modelo1.fit(X_train, y_train)
pred1 = modelo1.predict(X_test)

print("\nprecio = %.4f * ingreso + %.4f" % (modelo1.coef_[0], modelo1.intercept_))
r2_simple = mostrar_metricas("Regresion simple", y_test, pred1)

# ---------- Parte 2: regresion multiple ----------
# aqui uso todas las columnas menos el precio
X2 = df.drop(columns=["precio"])
X2_train, X2_test, y2_train, y2_test = train_test_split(X2, y, test_size=0.2, random_state=7)

modelo2 = LinearRegression()
modelo2.fit(X2_train, y2_train)
pred2 = modelo2.predict(X2_test)

print("\nCoeficientes:")
for nombre, coef in zip(X2.columns, modelo2.coef_):
    print(" ", nombre, round(coef, 5))
print("  intercepto", round(modelo2.intercept_, 5))
r2_multi = mostrar_metricas("Regresion multiple", y2_test, pred2)

print("\nR2 simple -> multiple: %.3f -> %.3f" % (r2_simple, r2_multi))

# ---------- Graficas ----------
fig, ax = plt.subplots(1, 2, figsize=(13, 5))

# izquierda: los datos de prueba con la recta
ax[0].scatter(X_test["ingreso"], y_test, s=4, alpha=0.3, label="Datos reales")
x_linea = pd.DataFrame({"ingreso": np.linspace(X_test["ingreso"].min(), X_test["ingreso"].max(), 100)})
ax[0].plot(x_linea["ingreso"], modelo1.predict(x_linea), color="red", label="Recta")
ax[0].set_title("Regresion simple: ingreso vs precio")
ax[0].set_xlabel("Ingreso medio (decenas de miles USD)")
ax[0].set_ylabel("Precio medio (cientos de miles USD)")
ax[0].legend()

# derecha: real vs predicho, si fuera perfecto todo estaria sobre la diagonal
ax[1].scatter(y2_test, pred2, s=4, alpha=0.3)
ax[1].plot([0, 5], [0, 5], "r--", label="Prediccion perfecta")
ax[1].set_title("Regresion multiple: real vs predicho (R2=%.2f)" % r2_multi)
ax[1].set_xlabel("Precio real")
ax[1].set_ylabel("Precio predicho")
ax[1].legend()

plt.tight_layout()
plt.savefig("resultados_regresion.png", dpi=120)
plt.show()

# Nota: muchos precios estan topados en 5.0 ($500,000) por eso sale esa linea
# horizontal arriba en la grafica, y eso le quita precision al modelo lineal.
