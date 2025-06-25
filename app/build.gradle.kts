plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.phnloinhn"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.phnloinhn"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:deprecation")
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.legacy.support.v4)
    implementation(libs.activity)
// Firebase BoM (manages versions of all Firebase libraries below)
    implementation(libs.firebase.bom)

    // Firebase libraries (versionless when using BoM)
    implementation(libs.google.firebase.auth)
    // Ignore no version in AndroidTest
    androidTestImplementation(libs.firebase.auth)

    // FirebaseUI still requires explicit version
    implementation(libs.firebase.ui.auth)

    // Credential Manager support for FirebaseUI 8+
    implementation(libs.credentials.play.services.auth)

    // Optional: Google Sign-In ID (if used explicitly elsewhere)
    implementation(libs.googleid)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}