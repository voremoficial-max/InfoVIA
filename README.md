# InfoVia — Fase 3

Aplicación Android nativa para reportes comunitarios de **tránsito** y
**accidentes**. Esta entrega parte de la Fase 1 y añade el flujo completo
de creación y envío de reportes a **Firebase Cloud Firestore**, sin registro
tradicional para el usuario.

## Identidad

- **Nombre:** InfoVia
- **Icono:** imagen proporcionada para InfoVia, integrada como icono de la aplicación.
- **Package/Application ID:** `com.alertaciudadana.app` (se conserva para no romper continuidad con la Fase 1).
- **Versión:** `1.0-fase2`

## Qué se implementó en la Fase 2

### Flujo de reporte

El botón **Reportar** ya no es un marcador de "próximamente". Abre un formulario
donde el usuario puede:

- Elegir **Tránsito** o **Accidente**.
- Elegir nivel **Bajo**, **Medio** o **Alto**.
- Seleccionar/ajustar una ubicación mediante un selector visual.
- Usar la ubicación actual cuando el permiso está disponible.
- Enviar el reporte sin escribir una dirección.
- Recibir una confirmación después del envío.

La fecha y hora se generan automáticamente al enviar el reporte. El usuario no
puede escribirlas manualmente.

### Modelo de datos

Cada reporte utiliza:

- `id`
- `type`
- `level`
- `latitude`
- `longitude`
- `date`
- `time`
- `serverTimestamp`
- `status`

Los valores de tipo, nivel y estado se guardan como cadenas controladas:

- `type`: `transito` | `accidente`
- `level`: `bajo` | `medio` | `alto`
- `status`: `activo` | `expirado`

La vigencia se mantiene centralizada en `AppConfig.kt`:

- Tránsito: 60 minutos.
- Accidente: 120 minutos.

La lógica de expiración completa corresponde a la Fase 4.

## Firebase

Se incorporó:

- Firebase Cloud Firestore.
- Firebase Authentication anónima para que el usuario no tenga que crear una cuenta.
- Repositorio separado de la interfaz.
- ViewModel para el estado del formulario.
- Reglas iniciales en `firestore.rules`.
- `firebase.json` para las reglas de Firestore.

La documentación oficial de Firebase indica que, al usar el BoM, las versiones
individuales de las librerías Firebase no se declaran por separado. Este proyecto
usa Firebase BoM `32.8.1` y el plugin Google Services `4.5.0`.

### Configurar Firebase

1. Crea un proyecto en Firebase.
2. Agrega una aplicación Android con el package:
   `com.alertaciudadana.app`.
3. Descarga `google-services.json`.
4. Colócalo exactamente en:

```text
app/google-services.json
```

5. En Firebase Console habilita:
   - **Authentication → Sign-in method → Anonymous**.
   - **Firestore Database**.
6. Publica las reglas contenidas en `firestore.rules`.

El archivo `app/google-services.json` está ignorado por Git para evitar subir
credenciales/configuración del proyecto por accidente. Se incluye
`app/google-services.json.example` únicamente como referencia de estructura.

### Compilación sin Firebase configurado

El proyecto está preparado para que **también pueda compilarse en GitHub Actions
sin `google-services.json`**. El plugin Google Services solo se aplica cuando el
archivo existe.

Si el APK se instala sin configurar Firebase, la interfaz sigue funcionando,
pero al intentar enviar un reporte se mostrará un mensaje indicando que Firebase
debe configurarse.

## Términos y condiciones

Los términos fueron ampliados y redactados con una estructura más formal.
Ahora cubren, entre otros:

- finalidad informativa;
- naturaleza comunitaria de los reportes;
- responsabilidad del usuario;
- prohibición de reportes falsos;
- seguridad vial;
- emergencias y fuentes oficiales;
- uso de ubicación;
- tratamiento de datos mínimos;
- disponibilidad del servicio;
- uso indebido;
- cambios de condiciones;
- aceptación.

La aceptación se mantiene guardada localmente para no mostrar los términos en
cada apertura.

> Estos términos son texto funcional para la aplicación y no sustituyen la
> revisión de un profesional jurídico si InfoVia se publica comercialmente.

## Arquitectura

```text
app/src/main/java/com/alertaciudadana/app/
├── data/
│   ├── preferences/
│   │   └── PreferencesManager.kt
│   └── reports/
│       ├── FirebaseReportRepository.kt
│       ├── Report.kt
│       └── ReportViewModel.kt
├── navigation/
│   └── NavGraph.kt
├── ui/
│   ├── screens/
│   │   ├── welcome/
│   │   ├── terms/
│   │   └── main/
│   └── theme/
├── util/
│   ├── AppConfig.kt
│   └── ViewModelFactory.kt
└── MainActivity.kt
```

La separación deja preparada la Fase 3 para sustituir el área de mapa por el
mapa geográfico real y comenzar a escuchar los reportes almacenados en Firestore
en tiempo real.

## GitHub Actions

Workflow:

```text
.github/workflows/android-build.yml
```

El workflow:

1. descarga el repositorio;
2. configura JDK 17;
3. instala Gradle 8.7;
4. compila `assembleDebug`;
5. publica el APK como artefacto.

### Secret recomendado para GitHub Actions

Para que el APK generado por GitHub Actions pueda conectarse a Firebase sin
subir `google-services.json` al repositorio:

1. Abre **Settings → Secrets and variables → Actions** en tu repositorio.
2. Crea un Secret llamado exactamente:

```text
GOOGLE_SERVICES_JSON
```

3. Como valor, pega el contenido completo de tu `google-services.json`.
4. Ejecuta el workflow.

El workflow crea temporalmente `app/google-services.json` durante la compilación
y no lo conserva como artefacto.

Para una prueba de compilación sin Firebase también puedes omitir el Secret:
el proyecto seguirá compilando, pero el envío de reportes requerirá configurar
Firebase.

No subas claves privadas, keystores ni archivos de configuración de proyectos
que no quieras publicar.

## Requisitos técnicos

- Kotlin 1.9.24
- Android Gradle Plugin 8.5.2
- Gradle 8.7
- JDK 17
- compileSdk 34
- targetSdk 34
- minSdk 26
- Jetpack Compose
- MVVM
- Firebase BoM 32.8.1
- Firebase Authentication
- Firebase Cloud Firestore
- Fused Location Provider

## Alcance de las siguientes fases

### Fase 3

- Mapa geográfico real.
- Marcadores de tránsito y accidentes.
- Diferenciación de niveles.
- Lectura de Firestore.
- Actualización en tiempo real.
- Información al tocar un reporte.
- Filtrado de reportes expirados.
- Centrado en la ubicación actual.

### Fase 4

- Expiración automática.
- Optimización de consultas.
- Manejo avanzado de errores y desconexión.
- Protección contra spam.
- Mejoras visuales.
- Optimización del mapa y Firebase.
- Documentación final.

**Esta entrega debe probarse antes de comenzar la Fase 3.**


## Fase 3 — OpenStreetMap, ciudades y reportes en tiempo real

- Se eliminó la necesidad del SDK de Google Maps.
- El mapa utiliza OpenStreetMap mediante osmdroid.
- Se muestra atribución visible a OpenStreetMap.
- El usuario selecciona una ciudad de Colombia antes de consultar el mapa.
- El mapa queda limitado al área configurada de la ciudad seleccionada.
- Los reportes se escuchan en tiempo real desde Firestore y se filtran por ciudad y estado activo.
- Tránsito usa centro azul y accidente centro verde.
- Bajo/Medio/Alto se representan mediante el borde amarillo/naranja/rojo.
- Al tocar un reporte aparece su información y tiempo transcurrido.
- Todos los reportes tienen `expiresAt` a 2 horas de su creación.

### Eliminación automática después de 2 horas

La aplicación deja de mostrar inmediatamente los reportes cuyo `expiresAt` ya venció. Para que Firestore elimine físicamente el documento de forma automática, en Firebase Console debe habilitarse una política TTL para el campo `expiresAt` de la colección `reports`. La arquitectura ya deja ese campo preparado.

### OpenStreetMap

El proyecto usa tiles HTTPS de OpenStreetMap y configura un User-Agent propio. Debe mantenerse la atribución visible y respetarse la política de uso de tiles de OpenStreetMap.

## Fase 4 — versión 1.0 y APK firmado

Esta versión corresponde al cierre de la primera versión funcional de InfoVia.

### Cambios de Fase 4

- Expiración visible y automática de reportes después de 2 horas.
- Eliminación de reportes vencidos de la lista en memoria sin esperar una nueva actualización de Firestore.
- Límite anti-spam de 30 segundos entre reportes enviados desde el mismo dispositivo.
- Validación de coordenadas antes de guardar reportes.
- Estados visuales para carga, errores y pérdida de conexión.
- Botón de reintento cuando no se pueden cargar los reportes.
- Optimización de actualización de marcadores OSM para evitar reconstruirlos cuando los datos no cambiaron.
- Términos y condiciones con presentación limpia y separada por secciones.
- Menú superior de tres puntos con Información y Donar.
- QR de donación completo y botón para copiar rápidamente la llave `@bold3150736053`.
- Versión de aplicación: **V1.0**.
- El workflow genera un APK release firmado con el nombre **InfoVia-v1.0.apk**.
- El artefacto de GitHub Actions se llama **InfoVia-v1.0-release**.

### Firma segura en GitHub Actions

La clave privada **no se incluye en el repositorio**. El workflow recibe el keystore y sus credenciales mediante GitHub Actions Secrets, genera el APK release, verifica su firma y publica el APK como artefacto.

#### 1. Crear el keystore

En Android Studio puedes usar `Build > Generate Signed Bundle / APK > APK > Create new` y crear un archivo `.jks`. Guarda el archivo y sus contraseñas en un lugar seguro.

También puedes conservar el mismo keystore para futuras versiones. No debes perderlo si quieres publicar actualizaciones firmadas de la misma aplicación.

#### 2. Convertir el `.jks` a Base64

En Linux/macOS:

```bash
base64 -w 0 InfoVia-release.jks > keystore.base64.txt
```

En Windows PowerShell:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("InfoVia-release.jks")) | Set-Content keystore.base64.txt
```

#### 3. Crear los Secrets en GitHub

En el repositorio abre `Settings > Secrets and variables > Actions > New repository secret` y crea:

- `SIGNING_KEYSTORE_BASE64` — contenido completo de `keystore.base64.txt`.
- `SIGNING_STORE_PASSWORD` — contraseña del keystore.
- `SIGNING_KEY_ALIAS` — alias de la clave.
- `SIGNING_KEY_PASSWORD` — contraseña de la clave.

GitHub almacena estos valores como secretos cifrados y el workflow solo los utiliza durante la ejecución.

#### 4. Ejecutar la compilación

Ve a `Actions > InfoVia v1.0 - APK firmado > Run workflow` y ejecuta la rama `main`.

El workflow:

1. Configura Java 17.
2. Configura Gradle.
3. Prepara Firebase.
4. Recupera el keystore desde el Secret.
5. Compila `release`.
6. Firma el APK.
7. Verifica la firma con `apksigner`.
8. Renombra el resultado a `InfoVia-v1.0.apk`.
9. Lo publica como artefacto `InfoVia-v1.0-release`.

**Nunca subas el `.jks`, las contraseñas ni `keystore.base64.txt` al repositorio.**


## Fase 4.1 — Actualizaciones automáticas y optimización

- InfoVia consulta periódicamente la última GitHub Release y avisa dentro de la aplicación cuando existe una versión superior.
- El botón **Buscar actualización** permite comprobar manualmente.
- **Actualizar ahora** abre el APK oficial de la Release para que Android gestione la instalación/confirmación.
- GitHub Actions usa automáticamente el repositorio donde se está ejecutando el workflow y crea/actualiza una Release `v<versionName>` con el APK firmado.
- Para publicar una nueva versión, aumenta `versionCode` y `versionName` en `app/build.gradle.kts` y sube los cambios a `main`.
- La escucha de Firestore se limita a `city + status=activo + expiresAt futuro`, reduciendo documentos transferidos.
- La escucha se detiene cuando la pantalla deja de estar visible y se reactiva al volver, evitando tráfico de Firestore en segundo plano.
- Se añadió el índice compuesto necesario para la consulta optimizada.
- El filtrado local de vencimiento se conserva para que los reportes desaparezcan de la interfaz exactamente a las 2 horas.

### Publicar una actualización

1. Cambia `versionCode` (por ejemplo, `1` → `2`) y `versionName` (por ejemplo, `1.0` → `1.1`).
2. Haz commit y `git push` a `main`.
3. GitHub Actions compila, firma y verifica el APK.
4. El workflow publica automáticamente `InfoVia-v1.1.apk` en la Release `v1.1`.
5. Los usuarios con InfoVia instalada comprobarán periódicamente la Release y recibirán el aviso de actualización.

**Importante:** conserva siempre el mismo keystore de firma (`infovia-release.jks`) para las futuras actualizaciones.
