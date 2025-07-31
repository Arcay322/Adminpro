# Project Structure

## Root Level
```
├── app/                    # Main application module
├── gradle/                 # Gradle wrapper and version catalog
├── build.gradle.kts        # Root build configuration
├── settings.gradle.kts     # Project settings and modules
└── gradle.properties       # Gradle properties
```

## Application Module (`app/`)
```
app/
├── build.gradle.kts        # App-level build configuration
├── proguard-rules.pro      # ProGuard configuration
└── src/
    ├── main/               # Main source set
    ├── test/               # Unit tests
    └── androidTest/        # Instrumented tests
```

## Main Source Structure (`app/src/main/`)
```
main/
├── AndroidManifest.xml     # App manifest
├── java/com/example/admin_ingresos/
│   ├── MainActivity.kt     # Main activity and navigation setup
│   ├── AppDatabaseProvider.kt  # Database singleton provider
│   ├── data/               # Data layer (Room entities, DAOs, repository)
│   └── ui/                 # UI layer (Compose screens and ViewModels)
└── res/                    # Android resources
    ├── drawable/           # Vector drawables and icons
    ├── font/               # Inter font family files
    ├── mipmap-*/           # App launcher icons
    ├── values/             # Strings, colors, themes
    └── xml/                # Backup and data extraction rules
```

## Package Organization

### Data Layer (`data/`)
- **Entities**: `Category.kt`, `PaymentMethod.kt`, `Transaction.kt`
- **DAOs**: `CategoryDao.kt`, `PaymentMethodDao.kt`, `TransactionDao.kt`
- **Database**: `AppDatabase.kt` (Room database configuration)
- **Repository**: `TransactionRepository.kt` (data access abstraction)

### UI Layer (`ui/`)
Organized by feature modules:
- **`category/`**: Category management screens and ViewModels
- **`dashboard/`**: Main dashboard with charts and overview
- **`history/`**: Transaction history listing
- **`reports/`**: Analytics and reporting screens
- **`theme/`**: Compose theme configuration (Color, Theme, Type)
- **`transaction/`**: Add/edit transaction screens and components

## Naming Conventions
- **Package names**: lowercase with underscores (`admin_ingresos`)
- **Class names**: PascalCase (`MainActivity`, `TransactionDao`)
- **File names**: Match class names exactly
- **Compose screens**: End with `Screen` (`DashboardScreen`)
- **ViewModels**: End with `ViewModel` (`DashboardViewModel`)
- **Database entities**: Singular nouns (`Transaction`, `Category`)

## Architecture Patterns
- **Feature-based organization**: UI components grouped by feature
- **Separation of concerns**: Clear data/UI layer separation
- **Single Activity**: Navigation handled via Compose Navigation
- **ViewModel per screen**: Each screen has its own ViewModel
- **Repository pattern**: Data access through repository layer