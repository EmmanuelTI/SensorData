// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
}

buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.1")  // 🔥 Añade esta línea
        // Otras dependencias de classpath si las tienes...
    }
}