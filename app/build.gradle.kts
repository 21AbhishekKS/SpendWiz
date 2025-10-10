plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"

}

android {
    namespace = "com.spendwiz.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.spendwiz.app"
        minSdk = 23
        targetSdk = 35
        versionCode = 6
        versionName = "1.1.3"



        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }



    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    //Lottie Animations
    val lottieVersion = "6.0.1" // Update with latest version if needed
    implementation ("com.airbnb.android:lottie-compose:$lottieVersion")

    //navigation
    implementation ("androidx.navigation:navigation-compose:2.7.7")

    //Live data
    implementation("androidx.compose.runtime:runtime-livedata:1.6.7")

    //Room Database
    implementation ("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation ("androidx.room:room-ktx:2.6.1") // For Kotlin projects

    //Flow row
    implementation ("com.google.accompanist:accompanist-flowlayout:0.30.1")

    // Splash screen
    implementation ("androidx.core:core-splashscreen:1.0.1")

    //charts
    implementation("io.github.thechance101:chart:1.1.0")

    //Data store
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    //Status bar blending with app
    implementation ("com.google.accompanist:accompanist-systemuicontroller:0.32.0")

    //work manager for daily notifcaion
    implementation ("androidx.work:work-runtime-ktx:2.9.0")

    //material for dark theme
    implementation("com.google.android.material:material:1.11.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    // ViewModel KTX
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Google Drive API
    implementation("com.google.api-client:google-api-client-android:2.2.0")
    // FIX: Corrected the group ID from 'com.google.api.client' to 'com.google.http-client'
    implementation("com.google.http-client:google-http-client-android:1.42.3")
    implementation("com.google.apis:google-api-services-drive:v3-rev20220815-2.0.0") {
        exclude("org.apache.httpcomponents")
    }

    // Coroutines for async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // kotlinx serialization
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Add the ML Kit Text Recognition dependency
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")
// For CameraX integration
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-core:${cameraxVersion}")
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
    implementation("androidx.camera:camera-view:${cameraxVersion}")
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

// For Coil to display images from a URI
    implementation("io.coil-kt:coil-compose:2.5.0")

    // For SQLCipher Database Encryption
    implementation ("net.zetetic:android-database-sqlcipher:4.5.4")
    implementation ("androidx.sqlite:sqlite-ktx:2.4.0")

    // For Securely Storing the Encryption Key
    implementation ("androidx.security:security-crypto:1.1.0-alpha06")

    // AdMob
    implementation("com.google.android.gms:play-services-ads:23.1.0")
    // Accompanist to help draw the ad icon
    implementation("com.google.accompanist:accompanist-drawablepainter:0.34.0")

    //In app update (now only implemented immediate in app update)
    implementation("com.google.android.play:app-update-ktx:2.1.0")

    //To get time and date for below 26 API to remove required O annotation
    implementation ("com.jakewharton.threetenabp:threetenabp:1.4.6")

}

