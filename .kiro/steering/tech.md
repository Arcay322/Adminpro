# Technology Stack

## Build System
- **Gradle** with Kotlin DSL (`.gradle.kts` files)
- **Android Gradle Plugin** 8.12.0-alpha02
- **Kotlin** 2.0.21 with Compose compiler plugin

## Core Technologies
- **Android SDK**: Min SDK 27, Target SDK 36, Compile SDK 36
- **Kotlin**: Primary language with JVM target 11
- **Jetpack Compose**: Modern UI toolkit for native Android UI
- **Material Design 3**: UI components and theming

## Key Libraries
- **Room Database**: Local SQLite database with Kotlin coroutines support
  - `androidx.room:room-runtime:2.6.1`
  - `androidx.room:room-ktx:2.6.1` (Kotlin extensions)
- **Navigation Compose**: `androidx.navigation:navigation-compose:2.7.7`
- **MPAndroidChart**: `com.github.PhilJay:MPAndroidChart:3.1.0` for data visualization
- **Material Icons Extended**: `androidx.compose.material:material-icons-extended:1.6.7`
- **Inter Font Family**: Custom typography implementation

## Architecture
- **MVVM Pattern**: ViewModels for business logic, Compose for UI
- **Repository Pattern**: Data layer abstraction
- **Room Database**: Entity-DAO-Database structure
- **Dependency Injection**: Manual DI with database provider pattern

## Common Commands
```bash
# Build the project
./gradlew build

# Install debug APK
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Clean build
./gradlew clean

# Generate APK
./gradlew assembleDebug
./gradlew assembleRelease
```

## Development Setup
- Java 11 compatibility required
- Kotlin KAPT for Room annotation processing
- Compose BOM for version alignment
- ProGuard disabled in debug builds