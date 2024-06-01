plugins {
    alias(libs.plugins.kotlinSerialization)
    id("org.jetbrains.kotlin.android")
    id("com.android.library")
    id("maven-publish")
}

android {
    namespace = "com.yausername.youtubedl_android"
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

tasks.register<Jar>("androidBundledSourcesJar") {
    archiveClassifier = "sources"
    from(android.sourceSets.getByName("main").java.srcDirs, android.sourceSets.getByName("bundled").java.srcDirs)
}

tasks.register<Jar>("androidNonbundledSourcesJar") {
    archiveClassifier = "sources"
    from(android.sourceSets.getByName("main").java.srcDirs, android.sourceSets.getByName("nonbundled").java.srcDirs)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("bundledRelease") {
                from(components["bundledRelease"])
                groupId = "com.github.yausername.youtubedl_android"
                artifactId = "library"
                version = project.version.toString()
            }

            create<MavenPublication>("nonbundledRelease") {
                from(components["nonbundledRelease"])
                groupId = "com.github.yausername.youtubedl_android"
                artifactId = "library-nonbundled"
                version = project.version.toString()
            }
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(project(":common"))
    implementation(libs.appCompat)
    implementation(libs.coreKtx)
    implementation(libs.kotlinxSerializationJson)
    implementation(libs.ktorClientCore)
    implementation(libs.ktorClientAndroid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidJunit)
    androidTestImplementation(libs.espresso)

    implementation(libs.commonsIo)
}
