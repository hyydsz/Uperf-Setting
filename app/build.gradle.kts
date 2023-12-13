@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.wateregg.uperfsetting"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.wateregg.uperfsetting"
        minSdk = 33
        targetSdk = 34

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildToolsVersion = "34.0.0"
    ndkVersion = "26.1.10909125"
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation("androidx.core:core-ktx:1.12.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("io.github.litao0621:nifty-slider:1.4.6")
    implementation("io.github.litao0621:nifty-slider-effect:1.4.6")
}