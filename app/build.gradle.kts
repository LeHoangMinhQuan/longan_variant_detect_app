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

    // Firebase
    implementation(libs.firebase.bom)
    implementation(libs.google.firebase.auth)
    androidTestImplementation(libs.firebase.auth)
    implementation(libs.firebase.ui.auth)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    // TensorFlow Lite core
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    // TensorFlow Lite Support (cho tensor, xử lý ảnh…)
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    // TensorFlow Lite Metadata (đọc metadata của model)
    implementation("org.tensorflow:tensorflow-lite-metadata:0.4.4")
    // Nếu cần GPU delegate
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}