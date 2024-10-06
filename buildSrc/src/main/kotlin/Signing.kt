package com.yausername.youtubedl_android

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.plugins.signing.SigningExtension

internal fun Project.configureSigning() {
    val publishing = extensions.getByType<PublishingExtension>()

    extensions.getByType<SigningExtension>().run {
        useGpgCmd()
        sign(publishing.publications["release"])
    }
}