// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val kotlin_version by extra("1.7.22")
    repositories {
        google()
        mavenCentral()
        maven {
            isAllowInsecureProtocol = true
            url = uri("http://jcenter.bintray.com")
        }

    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.13.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

val versionMajor = 0
val versionMinor = 18
val versionPatch = 1
val versionBuild = 0 // bump for dogfood builds, public betas, etc.
val versionCode = versionMajor * 100000 + versionMinor * 1000 + versionPatch * 100 + versionBuild
val versionSuffix = providers.gradleProperty("versionSuffix").orNull ?: "-local16k2"
val defaultVersionName = "$versionMajor.$versionMinor.$versionPatch$versionSuffix"
val publishedVersion = providers.gradleProperty("version").orNull ?: defaultVersionName

val defaultPublishGroup = "io.github.junkfood02.youtubedl-android"
val jitpackOwnerGroup = providers.gradleProperty("group").orNull
val publishedGroup =
    if (System.getenv("JITPACK") == "true" && !jitpackOwnerGroup.isNullOrBlank()) {
        "$jitpackOwnerGroup.${rootProject.projectDir.name}"
    } else {
        defaultPublishGroup
    }

extra.apply {
    set("versionCode", versionCode)
    set("versionName", "$versionMajor.$versionMinor.$versionPatch")
    set("appCompatVer", "1.4.2")
    set("junitVer", "4.13.2")
    set("androidJunitVer", "1.1.3")
    set("espressoVer", "3.4.0")
    set("jacksonVer", "2.11.1")
    set("commonsIoVer", "2.5") // supports java 1.6
    set("commonsCompressVer", "1.12") // supports java 1.6
    set("coreKtxVer", "1.8.0")
}

allprojects {
    group = publishedGroup
    version = publishedVersion
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

tasks.register("packagePublishedArtifacts") {
    val librariesToPublish = listOf("common", "library", "aria2c", "ffmpeg")
    librariesToPublish.forEach {
        dependsOn(":$it:publishReleasePublicationToMavenRepository")
    }
    doLast {
        exec {
            workingDir = project.buildDir.resolve("staging-deploy")
            standardOutput = System.out
            errorOutput = System.err

            val publishedGroupPath = publishedGroup.replace('.', '/')

            val zipCommands = listOf(
                "zip",
                "-r",
                project.buildDir.resolve("archive-$publishedVersion.zip").absolutePath,
            ) + librariesToPublish.map { "$publishedGroupPath/$it/$publishedVersion" }

            commandLine(zipCommands)
        }
    }

}

tasks.register("publishAllToMavenLocal") {
    val librariesToPublish = listOf("common", "library", "aria2c", "ffmpeg")
    librariesToPublish.forEach {
        dependsOn(":$it:publishReleasePublicationToMavenLocal")
    }
}

tasks.register("publishToMavenLocal") {
    dependsOn("publishAllToMavenLocal")
}
