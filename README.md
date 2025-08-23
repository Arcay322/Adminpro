Admin_ingresos (CashFlow)

Aplicación Android para gestión de finanzas personales: registrar transacciones, categorías, presupuestos, metas de ahorro, reportes y exportaciones.

Descripción
-----------
Admin_ingresos es una app móvil escrita en Kotlin usando Jetpack Compose. Provee:

- Registro y categorización de transacciones (ingresos/gastos).
- Visualización de balances, gráficos por categoría y flujo semanal.
- Gestión de presupuestos y metas de ahorro.
- Exportación de datos (Excel/PDF) y tareas en background con WorkManager.

Arquitectura y capas
--------------------
- UI: Jetpack Compose (pantallas en `app/src/main/java/com/example/admin_ingresos/ui`).
- Navegación: Navigation Compose dentro de `MainActivity.kt`.
- Persistencia: Room (DAOs y Entities en `app/src/main/java/com/example/admin_ingresos/data`).
- Repositorios/ViewModels: capa intermedia que expone Flows/State para la UI.

Tecnologías y dependencias principales
-------------------------------------
- Kotlin + Jetpack Compose (material3)
- Room (room-runtime, ksp room-compiler)
- Navigation Compose
- WorkManager
- Coil para carga de imágenes
- MPAndroidChart y componentes Compose personalizados para visualizaciones
- Apache POI (poi-ooxml) para exportación a Excel (considerar tamaño de APK)

Requisitos (local)
------------------
- JDK 11
- Android SDK con API 34 (compileSdk/targetSdk = 34)
- Android Studio (recomendado): Electric Eel o más reciente
- Gradle (se usa el wrapper incluido)

Cómo compilar y ejecutar (PowerShell, desde la raíz del proyecto `Adminpro`)
---------------------------------------------------------------------
1. Compilar APK debug:

    .\gradlew.bat assembleDebug

2. Instalar en dispositivo/emulador conectado:

    .\gradlew.bat installDebug

3. Limpiar y reconstruir (útil si cambias dependencias o KSP):

    .\gradlew.bat clean; .\gradlew.bat assembleDebug

Notas importantes
-----------------
- KSP: Room usa KSP; la primera compilación puede tardar por la generación de código.
- Migraciones de Room: el proyecto incluye varias migraciones en `AppDatabase.kt` (version 12). Antes de actualizar la versión de la base de datos en producción, verifica que exista una ruta de migración válida para todas las versiones soportadas. Si no hay migración intermedia, puedes optar por `fallbackToDestructiveMigration()` si aceptas resetear datos.
- Export (Apache POI): Apache POI puede aumentar el tamaño del APK. Si el tamaño es crítico, considera exportar CSV o mover la lógica de export a un módulo separado o servicio backend.
- Licencias: revisa las licencias de terceros antes de publicar (especial cuidado con librerías con licencias copyleft).

Estructura de carpetas (resumen)
--------------------------------
app/
  src/main/java/com/example/admin_ingresos/
    ui/                # pantallas y componentes Compose
    data/              # entities, daos, repositorios
    viewmodel/         # viewmodels auxiliares
    MainActivity.kt
    SplashActivity.kt

Consejos para desarrollo y PRs
-----------------------------
- Añade pruebas unitarias para ViewModels core (Dashboard, TransactionHistory, SavingsGoal).
- Añade una prueba de migración de Room por cada migración crítica.
- Considera habilitar lint y un job de CI que haga './gradlew assembleDebug' en cada PR.

Posibles siguientes pasos que puedo implementar
---------------------------------------------
- Ejecutar una compilación CI local y reportar errores de build.
- Añadir un workflow de GitHub Actions para build automático.
- Generar un script de prueba para validar migraciones de Room.

Contacto y contribuciones
-------------------------


Licencia: revisa `LICENSES.md` en el repositorio para decisiones de distribución.
