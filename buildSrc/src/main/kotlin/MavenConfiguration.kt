import org.gradle.api.publish.maven.MavenPom

object MavenConfiguration {
    fun MavenPom.commonPomConfiguration() {
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