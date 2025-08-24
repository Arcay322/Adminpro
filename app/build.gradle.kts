plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.admin_ingresos"
    compileSdk = 34 // CAMBIO: Se ajusta a la versión soportada por AGP 8.4.2

    defaultConfig {
        applicationId = "com.example.admin_ingresos"
        minSdk = 27
        targetSdk = 34 // CAMBIO: Se ajusta para coincidir con compileSdk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {

    // Compose Reorderable para drag & drop en grids
    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    // Compose UI is BOM-managed via platform(libs.androidx.compose.bom)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    
    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")
    
    
    // Lucide Icons 
    implementation("com.composables:icons-lucide-android:1.1.0")
    
    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // PDF generation
    // Using Android's PdfDocument instead of iText to avoid AGPL licensing.
    
    // WorkManager for background tasks
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    // Apache POI for Excel export (lightweight components)
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    
    // Material Icons Extended (BOM-managed)
    implementation("androidx.compose.material:material-icons-extended")

    // Foundation and Lazy components (BOM-managed)
    implementation("androidx.compose.foundation:foundation")

    // Graphics layer (BOM-managed)
    implementation("androidx.compose.ui:ui-graphics")
    
    // Color Picker
    implementation("com.github.skydoves:colorpicker-compose:1.1.2")
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}