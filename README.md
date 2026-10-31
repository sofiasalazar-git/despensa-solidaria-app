# Despensa Solidaria – App Android (Kotlin + Jetpack Compose)

Cliente móvil de la API REST del backend (`despensa-solidaria-backend`). Usa los 11 endpoints.

## Pantallas
1. **Inicio**: resumen (productos, por vencer, bajo stock), acciones rápidas, últimos movimientos.
2. **Productos**: lista con búsqueda, filtro por categoría y ver desactivados.
3. **Detalle de producto**: existencia, vencimiento, historial, botones Entrada/Salida, editar, desactivar.
4. **Nuevo / Editar producto**: validaciones y selector de fecha.
5. **Registrar movimiento**: entrada o salida (muestra el 409 del servidor si no hay existencia).
6. **Movimientos**: historial con filtro Entradas/Salidas.
7. **Detalle de movimiento**: corrección de responsable y observación (PATCH).
8. **Alertas**: por vencer (3/7/15/30 días) y bajo stock.
9. **Ajustes del servidor**: cambiar la URL y probar la conexión.

**Indicador de conexión**: franja bajo la barra superior en TODAS las pantallas.
Verde + icono WiFi = conectado · Rojo + WiFi tachado = sin conexión (toca "Reintentar") · Ámbar = verificando.
Consulta `GET /api` cada 10 s (4 s si está caído) y las listas se recargan solas al reconectar.

## Cómo abrirla
1. Android Studio → **Open** → carpeta `DespensaSolidaria` → esperar el *Gradle Sync* (necesita internet la primera vez).
2. Inicia el backend: `npm start` (tu `.env` usa **PORT=3000**).
3. Emulador: ya funciona con `http://10.0.2.2:3000/api/`.
   Celular físico: Ajustes (engranaje en Inicio) → `http://IP-DEL-PC:3000/api/` (mismo WiFi; permitir el puerto en el firewall).
4. Run ▶.

Si tu API corre en otro puerto (por ejemplo 8080) cámbialo en Ajustes.
