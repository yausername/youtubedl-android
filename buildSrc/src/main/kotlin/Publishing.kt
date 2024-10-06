package com.yausername.youtubedl_android

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get


internal fun Project.configurePublish(id: String) {

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("release") {
                groupId = "io.github.junkfood02.youtubedl-android"
                artifactId = id
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
                url = uri(rootProject.buildDir.resolve("staging-deploy").absolutePath)
            }
        }
    }
}


internal fun MavenPom.commonPomConfiguration() {
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