import MavenConfiguration.commonPomConfiguration

plugins {
    id("signing")
    id("com.android.library")
    id("maven-publish")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.yausername.aria2c"
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


    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "io.github.junkfood02.youtubedl-android"
            artifactId = "aria2c"
            version = project.version.toString()

            afterEvaluate {
                from(components["release"])
            }

            pom {
                commonPomConfiguration()
            }
        }
    }

    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["release"])
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(project(":common"))
    implementation("androidx.core:core-ktx:${rootProject.extra["coreKtxVer"]}")
    compileOnly(project(":library"))

    implementation("androidx.appcompat:appcompat:${rootProject.extra["appCompatVer"]}")
    testImplementation("junit:junit:${rootProject.extra["junitVer"]}")
    androidTestImplementation("androidx.test.ext:junit:${rootProject.extra["androidJunitVer"]}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${rootProject.extra["espressoVer"]}")

    implementation("commons-io:commons-io:${rootProject.extra["commonsIoVer"]}")
}
