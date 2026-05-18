plugins {
    // Use the explicit plugin ID instead of the alias
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "eu.kanade.tachiyomi.extension.all.kimcartoon"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }

    sourceSets {
        getByName("main") {
            // Explicitly point to the manifest in the core module
            manifest.srcFile("core/src/main/AndroidManifest.xml")
            // Include source code
            java.srcDirs("src/main/kotlin")
        }
    }
}

dependencies {
    implementation(project(":core"))
}