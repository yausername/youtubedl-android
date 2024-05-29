// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    val kotlinVersion by extra("1.9.20")
    repositories {
        google()
        mavenCentral()
        maven {
            isAllowInsecureProtocol = true
            url = uri("http://jcenter.bintray.com")
        }
    }
    dependencies {
        classpath(libs.androidToolsBuildGradle)
        classpath(libs.kotlinGradlePlugin)

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

val versionMajor = 0
val versionMinor = 16
val versionPatch = 0
val versionBuild = 0 // bump for dogfood builds, public betas, etc.

val versionCode = versionMajor * 100000 + versionMinor * 1000 + versionPatch * 100 + versionBuild

extra.apply {
    set("versionCode", versionCode)
    set("versionName", "$versionMajor.$versionMinor.$versionPatch")
    set("appCompatVer", "1.6.1")
    set("junitVer", "4.13.2")
    set("androidJunitVer", "1.1.5")
    set("espressoVer", "3.5.1")
    set("commonsIoVer", "2.5") // supports java 1.6
    set("commonsCompressVer", "1.12") // supports java 1.6
    set("coreKtxVer", "1.12.0")
    set("kotlinxSerializationVer", "1.4.1")
    set("ktorversion", "2.3.6")
}

allprojects {
    group = "com.github.yausername"
    version = versionCode
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
