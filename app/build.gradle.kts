plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.1.10"
}

android {
    namespace = "com.efojug.chatwithme"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.efojug.chatwithme"
        minSdk = 33
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

            buildConfigField("String", "CHAT_SERVER_URL", "\"ws://192.168.5.6:4380/chat\"")
            buildConfigField("String", "REGISTER_SERVER_URL", "\"http://192.168.5.6:4380/register\"")
            buildConfigField("String", "LOGIN_SERVER_URL", "\"http://192.168.5.6:4380/login\"")
        }

        debug {
            buildConfigField("String", "CHAT_SERVER_URL", "\"ws://192.168.5.6:4380/chat\"")
            buildConfigField("String", "REGISTER_SERVER_URL", "\"http://192.168.5.6:4380/register\"")
            buildConfigField("String", "LOGIN_SERVER_URL", "\"http://192.168.5.6:4380/login\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)
}