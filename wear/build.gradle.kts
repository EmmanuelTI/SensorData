plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "equipo.dinamita.otys"
    compileSdk = 34

    defaultConfig {
        applicationId = "equipo.dinamita.otys"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.play.services.wearable)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.viewpager2)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    wearApp(project(":wear"))

    implementation("com.google.android.gms:play-services-wearable:19.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation("androidx.wear:wear:1.3.0")

    implementation(libs.androidx.viewpager2) // Para el carrusel con ViewPager2
    implementation("androidx.fragment:fragment-ktx:1.6.1") // Para usar fragmentos con ViewPager2
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1") // Para soporte de ciclo de vida
    implementation("androidx.wear:wear:1.3.0") // Componentes de Wear OS
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3") // Coroutines para servicios de Google Play
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")


    implementation ("com.google.android.gms:play-services-location:21.0.1")





    implementation ("androidx.appcompat:appcompat:1.6.1")



}