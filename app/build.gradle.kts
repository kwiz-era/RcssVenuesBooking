plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services) // Ensure this is correctly configured
}

android {
    namespace = "com.example.rcssvenues"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.rcssvenues"
        minSdk = 32
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets {
        getByName("main") {
            assets {
                srcDirs("src\\main\\assets\\RcssVenue")
            }
        }
    }
}

dependencies {
    // Core Android Libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase Libraries
    implementation(platform("com.google.firebase:firebase-bom:32.7.0")) // Firebase BoM (Bill of Materials)
    implementation("com.google.firebase:firebase-auth") // Firebase Authentication
    implementation("com.google.firebase:firebase-firestore") // Firestore
    implementation("com.google.firebase:firebase-database") // Realtime Database
    implementation("com.google.firebase:firebase-analytics-ktx") // Firebase Analytics

    // Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}