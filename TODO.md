# Lista de Tareas del Proyecto: Admin_ingresos
## Requerimientos Funcionales
- Registro de ingresos y gastos con categorización.
- Visualización de balance mensual (ingresos - gastos).
- Resúmenes visuales: tarjetas de ingresos/gastos y gráfico de dona de gastos por categoría.
- Listado de transacciones recientes y completas, con filtros y búsqueda.
- Gestión de categorías: añadir, editar y eliminar.
- Registro de transacciones con monto, categoría, descripción, fecha y método de pago.
- Reportes y análisis: gráficos de flujo, análisis por categoría y selector de periodo.
- Experiencia de usuario fluida y moderna, con animaciones sutiles.

## Requerimientos Técnicos
- Android nativo con Kotlin y Jetpack Compose.
- Persistencia local con Room (SQLite).
- Navegación entre pantallas con Navigation Compose.
- Integración de iconografía minimalista (Material Icons/Heroicons).
- Tipografía personalizada (Inter o Poppins).
- Diseño minimalista inspirado en Tailwind CSS.
- Animaciones fluidas en transiciones y formularios.
- Pruebas unitarias para lógica y persistencia.
- Cumplimiento de privacidad y permisos de datos.

## 1. Planificación y Configuración Inicial

## 2. Modelado de Datos y Persistencia
- [ ] Diseñar entidades Room: Transacción, Categoría, Método de Pago
- [ ] Implementar DAOs para CRUD de entidades
- [ ] Crear la base de datos Room y su inicialización

## 3. Arquitectura y Lógica de Negocio
- [ ] Definir ViewModels para cada pantalla principal
- [ ] Implementar repositorios para acceso a datos
- [ ] Configurar navegación entre pantallas (Navigation Compose)

## 4. UI/UX y Componentes Visuales
- [ ] Crear la pantalla principal (Dashboard)
  - [ ] Balance del mes
  - [ ] Tarjetas de ingresos/gastos
  - [ ] Gráfico de dona de gastos por categoría
  - [ ] Lista de transacciones recientes
  - [ ] FAB para añadir transacción
- [ ] Crear pantalla de registro de transacción
  - [ ] Tabs para tipo (Ingreso/Gasto)
  - [ ] Formulario con campos requeridos
  - [ ] Selector y gestión de categorías
  - [ ] Selector de fecha y método de pago
- [ ] Crear pantalla de historial de transacciones
  - [ ] Listado completo
  - [ ] Filtros por tipo, categoría, fecha
  - [ ] Barra de búsqueda
- [ ] Crear pantalla de reportes y análisis
  - [ ] Selector de periodo
  - [ ] Gráfico de barras ingresos vs gastos
  - [ ] Análisis por categoría

## 5. Estilo y Experiencia de Usuario
- [ ] Aplicar diseño minimalista y moderno (inspirado en Tailwind CSS)
- [ ] Integrar iconografía consistente
- [ ] Añadir animaciones sutiles en transiciones y formularios

## 6. Pruebas y Calidad
- [ ] Implementar pruebas unitarias (ViewModels, DAOs)
- [ ] Probar la aplicación en diferentes dispositivos y versiones de Android
- [ ] Corregir bugs y optimizar rendimiento

## 7. Preparación para Despliegue
- [ ] Configurar íconos y splash screen
- [ ] Revisar permisos y privacidad de datos
- [ ] Generar APK/AAB de producción
- [ ] Documentar la aplicación (README, instrucciones de uso)

## 8. Despliegue
- [ ] Registrar la app en Google Play Console
- [ ] Subir APK/AAB y completar la ficha de la aplicación
- [ ] Publicar la aplicación en Google Play Store
