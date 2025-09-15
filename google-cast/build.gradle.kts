import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.theoplayer.sample.playback.cast"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.theoplayer.sample.playback.cast"
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

    kotlin {
        compilerOptions {
            apiVersion = KotlinVersion.KOTLIN_2_0
            jvmTarget = JvmTarget.JVM_1_8
        }
    }
}

dependencies {
    implementation(project(":common"))
    implementation(libs.compose.runtime)
    implementation(libs.material)
    implementation(libs.appcompat)
    implementation(libs.theoplayer)
    implementation(libs.theoplayer.connector.cast)
}
