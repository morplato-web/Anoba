import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
}

ksp {
    arg("room.generateKotlin", "true")
    arg("room.schemaLocation", "$projectDir/schemas")
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.neval.anoba"
    compileSdk = 36

    signingConfigs {
        create("release") {
            storeFile = file("D:/anahtar/anoba_key.jks")
            storePassword = "anoba58"
            keyAlias = "anoba-key"
            keyPassword = "anoba58"
        }
    }

    defaultConfig {
        applicationId = "com.neval.anoba"
        minSdk = 24
        targetSdk = 36
        versionCode = 140
        versionName = "9.2.1"

        // tüm desteklenen diller
        resourceConfigurations += listOf("tr", "en", "ar", "fr", "ru", "sw", "af", "es", "it", "de", "pt")

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }
        val agoraAppId = localProperties.getProperty("agora.app.id") ?: ""
        buildConfigField("String", "AGORA_APP_ID", "\"$agoraAppId\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            ndk {
                debugSymbolLevel = "FULL"
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
        }
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.ripple)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.animation)
    implementation(libs.androidx.animation.core)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.runtime.saveable)
    implementation(libs.androidx.ui.geometry)
    implementation(libs.androidx.ui.unit)
    implementation(libs.androidx.ui.text)
    implementation(libs.androidx.ui.util)
    implementation(libs.androidx.ui.tooling.data)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.exifinterface)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.firebase.appcheck.ktx)

    debugImplementation(libs.firebase.appcheck.debug)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.coil.network.okhttp)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.core)
    implementation(libs.koin.androidx.navigation)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.play.services.auth) {
        exclude(group = "com.android.support", module = "support-v4")
    }

    implementation(libs.coil.video)
    implementation(libs.androidx.tools.core)
    implementation(libs.kotlinx.coroutines.play.services)

    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.transformer)
    implementation(libs.androidx.media3.effect)
    implementation(libs.androidx.media3.muxer)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.video)
    implementation(libs.androidx.camera.view)

    implementation(libs.lottie.compose)
    implementation(libs.play.app.update)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.paging)

    // Paging 3
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    // Agora
    implementation(libs.agora.video.sdk)

    // Gson
    implementation(libs.google.code.gson)

    // Google Sign-In & Credentials
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)
    testImplementation(libs.junit)
    debugImplementation(libs.androidx.ui.tooling)
}