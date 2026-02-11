buildscript {
    dependencies {
        classpath(libs.hilt.android.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.serialization) apply false
    alias(libs.plugins.jetbrains.kotlin.compose.compiler) apply false
    alias(libs.plugins.kotlin.ksp) apply false
}
