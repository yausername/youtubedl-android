plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 34
    defaultConfig {
        applicationId = "com.yausername.youtubedl_android_example"
        minSdk = 21
        targetSdk = 34
        versionName = "TEST"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.add("x86")
            abiFilters.add("x86_64")
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
        }
    }

    flavorDimensions.add("bundling")

    productFlavors {
        create("bundled") {
            dimension = "bundling"
        }
        create("nonbundled") {
            dimension = "bundling"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    splits.abi {
        isEnable = true
        reset()
        include("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
        isUniversalApk = true
    }

    namespace = "com.yausername.youtubedl_android_example"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(project(":library"))
    implementation(project(":ffmpeg"))
    implementation(project(":aria2c"))
    api(project(":common"))

    implementation(libs.appCompat)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(libs.coreKtx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidJunit)
    androidTestImplementation(libs.espresso)

    implementation(libs.rxandroid)
    implementation(libs.exomedia)
}