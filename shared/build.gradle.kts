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

    // JVM Desktop (Windows / Mac) hedefi
    jvm("desktop") {
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
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.1.0")
            implementation("io.ktor:ktor-client-core:3.0.0")
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
            implementation("io.ktor:ktor-client-darwin:3.0.0")
        }
        getByName("desktopMain") {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("io.ktor:ktor-client-cio:3.0.0")
            }
        }
    }
}

compose.desktop {
    application {
        javaHome = "C:/Program Files/Java/jdk-24"
        mainClass = "com.neval.anoba.community.desktop.MainKt"
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe
            )
            packageName = "AnobaFreeHub"
            packageVersion = "9.2.7"
            description = "Anoba FreeHub Desktop - Oyun ve Topluluk Merkeziniz"
            copyright = "© 2026 Anoba"
            vendor = "Anoba"
            windows {
                iconFile.set(project.file("src/desktopMain/resources/anoba_logo.ico"))
                shortcut = true
                menu = true
                menuGroup = "Anoba"
            }
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
