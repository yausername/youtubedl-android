plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.yausername.youtubedl_android_example"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yausername.youtubedl_android_example"
        minSdk = 24
        targetSdk = 34
        versionName = rootProject.ext["versionName"] as String
        versionCode = rootProject.ext["versionCode"] as Int
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.add("x86")
            abiFilters.add("x86_64")
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
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

}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(project(":library"))
    implementation(project(":ffmpeg"))
    implementation(project(":aria2c"))

    implementation("androidx.appcompat:appcompat:${rootProject.extra["appCompatVer"]}")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.core:core-ktx:${rootProject.extra["coreKtxVer"]}")
    testImplementation("junit:junit:${rootProject.extra["junitVer"]}")
    androidTestImplementation("androidx.test.ext:junit:${rootProject.extra["androidJunitVer"]}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${rootProject.extra["espressoVer"]}")

    implementation("io.reactivex.rxjava2:rxandroid:2.1.0")
    implementation("com.devbrackets.android:exomedia:5.1.0")
}
