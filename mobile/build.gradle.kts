plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services") // 🔥 Añade el plugin de Google Services
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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.play.services.wearable)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    wearApp(project(":wear"))

    implementation("com.google.android.gms:play-services-wearable:19.0.0")
    implementation("androidx.cardview:cardview:1.0.0")

    implementation("com.google.android.gms:play-services-wearable:17.1.0")


    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation ("com.google.android.material:material:1.12.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")


    implementation ("org.osmdroid:osmdroid-android:6.1.14")

    implementation ("androidx.preference:preference-ktx:1.2.0")


    implementation ("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
    implementation ("com.google.android.gms:play-services-wearable:18.0.0")

    // Firebase BoM (Bill of Materials) - Maneja versiones compatibles automáticamente
    implementation(platform("com.google.firebase:firebase-bom:32.8.1")) // 🔥 Versión más reciente
    implementation("com.google.firebase:firebase-analytics") // Si necesitas Analytics
    implementation("com.google.firebase:firebase-firestore") // Si usas Firestore
    implementation("com.google.firebase:firebase-auth") // Si necesitas autenticación
    // Firestore
    implementation("com.google.firebase:firebase-firestore-ktx") // Para Kotlin

    implementation ("com.google.android.material:material:1.11.0 ")// o versión actual

    // Opcional: Si usas autenticación
    //implementation("com.google.firebase:firebase-auth-ktx")
}