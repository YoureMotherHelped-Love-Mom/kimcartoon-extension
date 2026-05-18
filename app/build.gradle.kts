plugins {
    // Change to library plugin
    alias(libs.plugins.android.library) 
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "eu.kanade.tachiyomi.extension.all.kimcartoon"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }

    sourceSets {
        getByName("main") {
            // Point to the correct source and manifest paths
            manifest.srcFile("core/src/main/AndroidManifest.xml")
            java.srcDirs("src/main/kotlin")
        }
    }
}

dependencies {
    implementation(project(":core"))
}