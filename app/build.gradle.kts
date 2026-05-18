plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "eu.kanade.tachiyomi.extension.all.kimcartoon"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        applicationId = "eu.kanade.tachiyomi.extension.all.kimcartoon"
        versionCode = 1
        versionName = "1.0.0"
        manifestPlaceholders["extClass"] = "eu.kanade.tachiyomi.extension.all.kimcartoon.KimCartoon"
        manifestPlaceholders["nsfw"] = false
        manifestPlaceholders["appName"] = "KimCartoon"
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "android"
            keyAlias = System.getenv("KEY_ALIAS") ?: "androiddebugkey"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "android"
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/main/AndroidManifest.xml")
            java.srcDirs("src/main/kotlin", "../src/main/kotlin")
        }
    }
}

dependencies {
    // Aniyomi extension API (compileOnly — provided by the host app at runtime)
    compileOnly("com.github.aniyomiorg:extensions-lib:v16")

    // Core module (resources, launcher icons)
    implementation(project(":core"))

    // Runtime dependencies (some may be provided transitively by extensions-lib)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("com.squareup.okio:okio:3.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.6.0")
}
