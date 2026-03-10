import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.jetbrains.kotlin.compose.compiler)
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn"
    compileSdk = 36

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(localPropertiesFile.inputStream())
    }
    defaultConfig {
        applicationId = "com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn"
        minSdk = 28 // WireGuard requires minSdk 21+, but 26 is safer for modern features
        targetSdk = 36
        versionCode = 3
        versionName = "1.0.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "APP_OPEN_AD_ID", "\"ca-app-pub-3940256099942544/9257395921\"")
            buildConfigField("String", "BANNER_AD_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("String", "INTERSTITIAL_AD_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "REMOVE_ADS_SKU", "\"android.test.purchased\"")
            buildConfigField("Boolean", "ADS_DISABLED", "false")
            manifestPlaceholders["caAppPubId"] = "ca-app-pub-3940256099942544~3347511713"
        }
        release {
            isMinifyEnabled = true
            buildConfigField("String", "APP_OPEN_AD_ID", "\"${localProperties.getProperty("APP_OPEN_AD_ID")}\"")
            buildConfigField("String", "BANNER_AD_ID", "\"${localProperties.getProperty("BANNER_AD_ID")}\"")
            buildConfigField("String", "INTERSTITIAL_AD_ID", "\"${localProperties.getProperty("INTERSTITIAL_AD_ID")}\"")
            buildConfigField("String", "REMOVE_ADS_SKU", "\"test\"")
            buildConfigField("Boolean", "ADS_DISABLED", "false")
            manifestPlaceholders["caAppPubId"] = "ca-app-pub-9439921169677011~9566091994"
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    // composeOptions removed as it's not needed for Kotlin 2.0+ with the new plugin
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // Missing dependencies for ViewModel and Icons
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    
    // Networking
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)

    // WireGuard
    //noinspection Aligned16KB
    implementation(libs.tunnel) // Updated
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Google Play Billing
    implementation(libs.billing.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Hilt
    implementation(libs.hilt.android.v2481)
    ksp(libs.hilt.android.compiler.v2481)
    implementation(libs.androidx.hilt.navigation.compose.v120)

    // Ads
    implementation(project(":ads"))
    implementation(libs.googleAds)
}
