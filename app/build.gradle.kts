plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
//    id("com.google.gms.google-services")
}

android {
    namespace = "com.ubasangg.mathdrills"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ubasangg.mathdrills"
        minSdk = 26
        targetSdk = 35
        versionCode = 18
        versionName = "1.18"

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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.activity:activity:1.10.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.1")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.1")
    implementation("com.google.android.gms:play-services-ads:24.4.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation("com.google.code.gson:gson:2.13.1")
//    implementation(platform("com.google.firebase:firebase-bom:32.6.0"))

    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.29")
}