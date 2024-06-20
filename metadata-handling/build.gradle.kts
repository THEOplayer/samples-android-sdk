plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.theoplayer.sample.playback.metadata"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.theoplayer.sample.playback.metadata"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    dataBinding {
        enable = true
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
}

dependencies {
    implementation(project(":common"))
    implementation(libs.material)

    // THEOplayer required dependencies.
    implementation(libs.theoplayer)
    implementation(libs.gson)
}
