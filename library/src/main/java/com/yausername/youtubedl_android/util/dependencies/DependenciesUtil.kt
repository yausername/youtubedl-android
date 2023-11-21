package com.yausername.youtubedl_android.util.dependencies

import android.content.Context
import com.yausername.youtubedl_android.Constants
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.domain.CpuArchitecture
import com.yausername.youtubedl_android.domain.Dependency
import com.yausername.youtubedl_android.domain.Dependency.Companion.toDirectoryName
import com.yausername.youtubedl_android.domain.Dependency.Companion.toLibraryName
import com.yausername.youtubedl_android.domain.model.updates.Release
import com.yausername.youtubedl_android.util.network.Ktor.client
import com.yausername.youtubedl_android.util.network.Ktor.makeApiCall
import com.yausername.youtubedl_common.utils.ZipUtils.unzip
import org.apache.commons.io.FileUtils
import java.io.File

object DependenciesUtil {
    fun deleteDependency(dependency: Dependency): Boolean {
        val dependencyFile = File(YoutubeDL.binariesDirectory, dependency.toLibraryName())
        return FileUtils.deleteQuietly(dependencyFile)
    }

    fun unzipToDependencyDirectory(context: Context, tempFile: File, dependency: Dependency) {
        val baseDir = File(context.noBackupFilesDir, Constants.LIBRARY_NAME)
        val packagesDir = File(baseDir, Constants.PACKAGES_ROOT_NAME)

        val dependencyFile = File(packagesDir, dependency.toDirectoryName())
        unzip(tempFile, dependencyFile)
    }

    //The dependency scheme is: [arch]_lib[dependencyName].zip.so
    private const val LATEST_RELEASE = "https://api.github.com/repos/bobbyesp/youtubedl-android/releases/latest"

    @Throws(Exception::class)
    suspend fun getRelease(releaseUrl: String = LATEST_RELEASE): Release? {
        return makeApiCall<Release?>(client, releaseUrl, null)
    }

    @Throws(Exception::class)
    fun getDownloadLinkForDependency(architecture: CpuArchitecture, dependency: Dependency, release: Release): String {
        val dependencyName = dependency.toString()
        val desiredArch = architecture.name.lowercase()
        val dependencyFileName = "${desiredArch}_lib$dependencyName.zip.so"
        val dependencyAsset = release.assets.find { it.name.lowercase() == dependencyFileName }

        return dependencyAsset?.browser_download_url ?: throw Exception("dependency $dependencyName not found for architecture ${architecture.name}")
    }

    @Throws(Exception::class)
    suspend fun getDownloadLinkForDependency(architecture: CpuArchitecture, dependency: Dependency): String {
        val release = getRelease() ?: throw Exception("No release found")
        return getDownloadLinkForDependency(architecture, dependency, release)
    }
}