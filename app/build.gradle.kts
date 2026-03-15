plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.omniconverter.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.omniconverter.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        multiDexEnabled = true

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
    implementation("androidx.multidex:multidex:2.0.1")

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

    // Apache POI for Word documents
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")

    // ML Kit Text Recognition (OCR)
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")


    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}