plugins {
    id("signing")
    id("com.android.library")
    id("maven-publish")
    id("org.jetbrains.kotlin.android")
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
            artifactId = "library"
            version = project.version.toString()

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("youtubedl-android")
                description.set("youtube-dl for Android")
                url.set("https://github.com/yausername/youtubedl-android")
                inceptionYear.set("2019")
                licenses {
                    license {
                        name.set("GPL-3.0 license")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
                    }
                }
                developers {
                    developer {
                        id.set("yausername")
                        name.set("yausername")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/yausername/youtubedl-android")
                    url.set("https://github.com/yausername/youtubedl-android")
                }
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

    implementation("androidx.appcompat:appcompat:${rootProject.extra["appCompatVer"]}")
    implementation("androidx.core:core-ktx:${rootProject.extra["coreKtxVer"]}")
    testImplementation("junit:junit:${rootProject.extra["junitVer"]}")
    androidTestImplementation("androidx.test.ext:junit:${rootProject.extra["androidJunitVer"]}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${rootProject.extra["espressoVer"]}")

    implementation("com.fasterxml.jackson.core:jackson-databind:${rootProject.extra["jacksonVer"]}")
    implementation("com.fasterxml.jackson.core:jackson-annotations:${rootProject.extra["jacksonVer"]}")
    implementation("commons-io:commons-io:${rootProject.extra["commonsIoVer"]}")
}
