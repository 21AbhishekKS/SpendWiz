plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.abhi.expencetracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.abhi.expencetracker"
        minSdk = 23
        targetSdk = 35
        versionCode = 12
        versionName = "1.5.9"



        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
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


}



