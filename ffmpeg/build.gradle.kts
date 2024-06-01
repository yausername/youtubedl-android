plugins {
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
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    namespace = "com.yausername.ffmpeg"

    sourceSets {
        getByName("nonbundled") {
            java.srcDir("src/nonbundled/java")
            jniLibs.srcDirs("src/nonbundled/jniLibs")
        }
        getByName("bundled") {
            java.srcDir("src/bundled/java")
            jniLibs.srcDirs("src/bundled/jniLibs")
        }
    }

    publishing {
        singleVariant("bundledRelease") {
            withSourcesJar()
            withJavadocJar()
        }
        singleVariant("nonbundledRelease") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("bundledRelease") {
                from(components["bundledRelease"])
                groupId = "com.github.yausername.youtubedl_android"
                artifactId = "ffmpeg"
                version = project.version.toString()
            }

            create<MavenPublication>("nonbundledRelease") {
                from(components["nonbundledRelease"])
                groupId = "com.github.yausername.youtubedl_android"
                artifactId = "ffmpeg-nonbundled"
                version = project.version.toString()
            }
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(project(":common"))
    implementation(libs.coreKtx)
    compileOnly(project(":library"))

    implementation(libs.appCompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidJunit)
    androidTestImplementation(libs.espresso)

    implementation(libs.commonsIo)
}