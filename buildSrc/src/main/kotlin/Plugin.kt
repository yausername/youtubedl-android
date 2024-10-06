package com.yausername.youtubedl_android

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType

open class PublishConfigurationExtension {
    var isPublished: Boolean = false
    var artifactId: String = ""
}


open class PublishPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create<PublishConfigurationExtension>(
            "configurePublishing", PublishConfigurationExtension::class.java
        )
        project.afterEvaluate {
            project.extensions.getByType<PublishConfigurationExtension>().run {
                if (isPublished) {
                    println("Published!")
                    project.configureAndroid()
                    project.configurePublish(id = artifactId)
                    project.configureSigning()
                }
            }
        }
    }
}



