plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "keiyoushi.core"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }

    buildFeatures {
        resValues = false
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    // Replaced libs.bundles.common with direct references
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
    compileOnly("androidx.annotation:annotation:1.7.1")

    // Add these if your core uses OkHttp or JSoup (common in Tachiyomi)
    compileOnly("com.squareup.okhttp3:okhttp:4.12.0")
    compileOnly("org.jsoup:jsoup:1.17.2")

    // Replaced libs.junit
    testImplementation("junit:junit:4.13.2")
}