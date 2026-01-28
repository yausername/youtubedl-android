plugins {
    id("com.yausername.youtubedl_android")
    id("signing")
    id("com.android.library")
    id("maven-publish")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.yausername.youtubedl_common"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

configurePublishing {
    artifactId = project.name
    isPublished = true
}

dependencies {
    implementation("androidx.appcompat:appcompat:${rootProject.extra["appCompatVer"]}")
    implementation("androidx.core:core-ktx:${rootProject.extra["coreKtxVer"]}")
    testImplementation("junit:junit:${rootProject.extra["junitVer"]}")
    androidTestImplementation("androidx.test.ext:junit:${rootProject.extra["androidJunitVer"]}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${rootProject.extra["espressoVer"]}")

    implementation("commons-io:commons-io:${rootProject.extra["commonsIoVer"]}")
    implementation("org.apache.commons:commons-compress:${rootProject.extra["commonsCompressVer"]}")
}
