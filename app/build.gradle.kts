plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "eu.kanade.tachiyomi.extension.all.kimcartoon"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }

    // JVM compatibility configuration inside the android block
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    sourceSets {
        getByName("main") {
            // Pointing to the manifest in the core module
            manifest.srcFile("core/src/main/AndroidManifest.xml")
            // Include source code from current module
            java.srcDirs("src/main/kotlin")
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
     implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
