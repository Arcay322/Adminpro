#!/bin/bash

# Script para solucionar problemas de compilación de Room/Kapt
# Ejecutar este script en la raíz del proyecto Admin_ingresos

echo "🔧 Solucionando problemas de compilación Room/Kapt..."

# 1. Limpiar completamente el build
echo "🧹 Limpiando build..."
./gradlew clean

# 2. Limpiar cache de Gradle
echo "🗑️ Limpiando cache de Gradle..."
./gradlew cleanBuildCache

# 3. Invalidar cache de Android Studio
echo "📱 Para Android Studio:"
echo "   - Ve a File > Invalidate Caches and Restart"
echo "   - Selecciona 'Invalidate and Restart'"

# 4. Rebuilding con flags específicos para Kapt
echo "🔨 Recompilando con configuración optimizada para Kapt..."
./gradlew assembleDebug --rerun-tasks --no-build-cache

echo "✅ Proceso completado!"
echo ""
echo "Si el problema persiste:"
echo "1. Asegúrate de tener la versión correcta de Java (JDK 11 o superior)"
echo "2. Verifica que las versiones de Room en build.gradle sean compatibles"
echo "3. Intenta compilar desde línea de comandos: ./gradlew assembleDebug"
