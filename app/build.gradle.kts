plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.omniconverter.app"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.omniconverter.app"
        minSdk = 24
        targetSdk = 36
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Fragment and RecyclerView
    implementation("androidx.fragment:fragment:1.5.7")
    implementation("androidx.recyclerview:recyclerview:1.3.0")

    // Core libraries for conversion app
    implementation("androidx.work:work-runtime:2.8.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("androidx.room:room-runtime:2.5.2")
    annotationProcessor("androidx.room:room-compiler:2.5.2")

    // PDFBox android (placeholder) - correct group id with dash
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")


    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}