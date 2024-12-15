import java.util.Properties
import java.io.FileInputStream
plugins {
    kotlin("kapt")
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")

}

val keystorePropertiesFile = rootProject.file("keystore.properties")

// Initialize a new Properties() object called keystoreProperties.
val keystoreProperties = Properties()

// Load your keystore.properties file into the keystoreProperties object.
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

android {
    namespace = "com.example.naisupho"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.naisupho"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures{
        viewBinding = true
        buildConfig = true
    }

    signingConfigs {
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Định nghĩa các API keys từ keystoreProperties
            buildConfigField("String", "GOOGLE_Martrix_API", "\"${keystoreProperties["GOOGLE_Martrix_API"]}\"")
            buildConfigField("String", "PAYPAY_SECRET_API", "\"${keystoreProperties["PAYPAY_SECRET_API"]}\"")
            buildConfigField("String", "google_app_id", "\"${keystoreProperties["google_app_id"]}\"")
            buildConfigField("String", "google_api_key", "\"${keystoreProperties["google_api_key"]}\"")
            buildConfigField("String", "gcm_defaultSenderId", "\"${keystoreProperties["gcm_defaultSenderId"]}\"")
            buildConfigField("String", "default_web_client_id", "\"${keystoreProperties["default_web_client_id"]}\"")
        }
        debug {
            // Nếu cần dùng API keys cho debug cũng có thể làm tương tự
            buildConfigField("String", "GOOGLE_Martrix_API", "\"${keystoreProperties["GOOGLE_Martrix_API"]}\"")
            buildConfigField("String", "PAYPAY_SECRET_API", "\"${keystoreProperties["PAYPAY_SECRET_API"]}\"")
            buildConfigField("String", "google_app_id", "\"${keystoreProperties["google_app_id"]}\"")
            buildConfigField("String", "google_api_key", "\"${keystoreProperties["google_api_key"]}\"")
            buildConfigField("String", "gcm_defaultSenderId", "\"${keystoreProperties["gcm_defaultSenderId"]}\"")
            buildConfigField("String", "default_web_client_id", "\"${keystoreProperties["default_web_client_id"]}\"")
        }
    }
    packagingOptions {
        exclude("META-INF/NOTICE.md")
        exclude("META-INF/LICENSE.md")
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
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.play.services.maps)
    implementation(libs.firebase.database.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    //Glide
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    kapt("com.github.bumptech.glide:compiler:4.12.0")

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-storage:20.0.0")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")
    implementation ("com.google.android.gms:play-services-location:18.0.0")
    implementation ("com.google.android.material:material:1.4.0")
    implementation ("com.google.firebase:firebase-auth:21.3.0")

    //Image Slider
    implementation ("com.github.denzcoskun:ImageSlideshow:0.1.2")

    //Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    //OkHttp
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")


    //Dagger Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")

    //Paypay SDK
    implementation("org.hibernate.validator:hibernate-validator-annotation-processor:8.0.1.Final")
    implementation ("jp.ne.paypay:paypayopa:1.0.8")

}