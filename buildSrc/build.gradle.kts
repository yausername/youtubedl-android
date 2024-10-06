plugins {
    `kotlin-dsl`
    id("signing")
    id("maven-publish")
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("com.android.tools.build:gradle:7.1.3")
    implementation(gradleApi())
    implementation(localGroovy())
}

gradlePlugin {
    plugins {
        create("PublishPlugin") {
            id = "com.yausername.youtubedl_android"
            implementationClass = "com.yausername.youtubedl_android.PublishPlugin"
        }
    }
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8