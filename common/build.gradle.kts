plugins {
    alias(libs.plugins.kotlinSerialization)
    id("org.jetbrains.kotlin.android")
    id("com.android.library")
    id("maven-publish")
}

android {
    compileSdk = 34
    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    namespace = "com.yausername.youtubedl_common"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    publishing {
        singleVariant("release") {
            withJavadocJar()
            withSourcesJar()
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.yausername.youtubedl_android"
                artifactId = "common"
                version = project.version.toString()
            }
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(libs.appCompat)
    implementation(libs.coreKtx)
    implementation(libs.kotlinxSerializationJson)
    implementation(libs.ktorClientCore)
    implementation(libs.ktorClientAndroid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidJunit)
    androidTestImplementation(libs.espresso)

    implementation(libs.commonsIo)
    implementation(libs.commonsCompress)
}