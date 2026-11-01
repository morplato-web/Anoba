plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    // Android hedefi
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    // iOS hedefleri (XCFramework çıktısı için)
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SharedKit"
            isStatic = true
        }
    }
    
    // Kaynak dizinleri ve ortak bağımlılıklar
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.androidx.paging.common)
            implementation(libs.androidx.room.paging)
            implementation(libs.coil.compose)
            implementation(libs.koin.compose)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.runtime.compose)
            implementation(compose.materialIconsExtended)
        }
        androidMain.dependencies {
            implementation(libs.androidx.material.icons.core)
            implementation(libs.androidx.material.icons.extended)
        }
        iosMain.dependencies {
            // iOS'a özel bağımlılıklar buraya gelecek
        }
    }
}

// Room Gradle Plugin (androidx.room) room-compiler bağımlılığını otomatik yönetir.

android {
    namespace = "com.anoba.shared"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
