include(":common", ":app", ":library", ":ffmpeg", ":aria2c", ":app_compose")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        jcenter()
        // e.g this is how you would add jitpack
        maven { url = uri("https://jitpack.io") }
        // Add any repositories you would be adding to all projects here
    }
}