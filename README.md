# 🚦 InfoVia

**InfoVia** es una aplicación Android orientada a la comunidad que permite reportar y consultar incidentes de **tránsito** y **accidentes** en tiempo real sobre un mapa.

La aplicación utiliza **OpenStreetMap** para la visualización del mapa y **Firebase Firestore** para almacenar y sincronizar los reportes.

---

## 📱 Características principales

### 🗺️ Mapa en tiempo real

- Mapa basado en OpenStreetMap.
- Visualización de reportes realizados por los usuarios.
- Actualización de reportes en tiempo real.
- El mapa se centra inicialmente en la ubicación del usuario cuando está disponible.
- La navegación está limitada a la ciudad seleccionada.
- Los reportes aparecen mediante marcadores diferenciados.

### 🚦 Reportes de tránsito

Permite registrar situaciones relacionadas con tránsito.

Los reportes pueden tener diferentes niveles de intensidad:

- 🟡 **Bajo**
- 🟠 **Medio**
- 🔴 **Alto**

### 🚑 Reportes de accidentes

Permite informar accidentes encontrados en la vía.

Los accidentes también utilizan los niveles:

- 🟡 **Bajo**
- 🟠 **Medio**
- 🔴 **Alto**

### 📍 Ubicación del reporte

Al crear un reporte:

- Se utiliza inicialmente la ubicación actual.
- El usuario puede mover manualmente la ubicación.
- También puede seleccionar otra posición directamente sobre el mapa.
- La ubicación queda asociada al reporte.

### ⏱️ Duración de los reportes

Los reportes tienen una duración máxima de **2 horas**.

Una vez superado ese tiempo, la aplicación deja de mostrar el reporte como activo.

Esto evita mantener información antigua en el mapa.

---

# 🔥 Firebase

InfoVia utiliza **Firebase Firestore** como base de datos.

Los reportes almacenan información como:

- Tipo de reporte.
- Nivel de intensidad.
- Latitud.
- Longitud.
- Ciudad.
- Fecha.
- Hora.
- Estado.
- Fecha de expiración.

La aplicación utiliza consultas optimizadas para obtener únicamente los reportes correspondientes a la ciudad seleccionada y que todavía estén activos.

---

## ⚡ Actualización automática

InfoVia cuenta con un sistema de actualización mediante **GitHub Releases**.

La aplicación puede comprobar si existe una versión más reciente.

### Comprobación automática

La aplicación realiza una comprobación periódica de nuevas versiones.

### Comprobación manual

También existe la opción:

**⋮ → Buscar actualización**

Si existe una versión nueva, InfoVia muestra la información de la actualización y permite abrir la descarga del APK.

### Publicación de nuevas versiones

Para publicar una nueva versión:

1. Cambiar `versionName`.
2. Incrementar `versionCode`.
3. Hacer commit de los cambios.
4. Subirlos a la rama `main`.

GitHub Actions se encarga de:

- Compilar el APK.
- Firmar el APK.
- Verificar la firma.
- Crear el GitHub Release.
- Subir el APK al Release.

---

# 🛡️ Seguridad y robustez

La aplicación incorpora diferentes mecanismos para evitar errores y abusos:

- Validación de coordenadas.
- Control básico contra reportes repetidos.
- Manejo de errores de Firebase.
- Indicador de conexión.
- Estado de carga.
- Mensajes de error y posibilidad de reintentar.
- Filtrado local de reportes expirados.
- Detención del listener de Firebase cuando la aplicación pasa a segundo plano.
- Consultas de Firestore optimizadas.

---

# 💰 Donaciones

InfoVia incluye una sección de donaciones.

Desde:

**⋮ → Donar**

se puede encontrar:

### Llave de donación

```text
@bold3150736053
