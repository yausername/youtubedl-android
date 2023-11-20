package com.yausername.youtubedl_android.util.plugins

import android.content.Context
import android.util.Log
import com.yausername.youtubedl_android.Constants
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.domain.CpuArchitecture
import com.yausername.youtubedl_android.domain.Plugin
import com.yausername.youtubedl_android.domain.Plugin.Companion.toDirectoryName
import com.yausername.youtubedl_android.domain.Plugin.Companion.toLibraryName
import com.yausername.youtubedl_android.domain.model.updates.Release
import com.yausername.youtubedl_android.util.network.Ktor.client
import com.yausername.youtubedl_android.util.network.Ktor.makeApiCall
import com.yausername.youtubedl_common.utils.ZipUtils.unzip
import org.apache.commons.io.FileUtils
import java.io.File

object PluginsUtil {
    fun deletePlugin(plugin: Plugin): Boolean {
        val pluginFile = File(YoutubeDL.binariesDirectory, plugin.toLibraryName())
        return FileUtils.deleteQuietly(pluginFile)
    }

    fun unzipToPluginDirectory(context: Context, tempFile: File, plugin: Plugin) {
        val baseDir = File(context.noBackupFilesDir, Constants.LIBRARY_NAME)
        val packagesDir = File(baseDir, Constants.PACKAGES_ROOT_NAME)

        val pluginFile = File(packagesDir, plugin.toDirectoryName())
        unzip(tempFile, pluginFile)
    }

    //The plugins scheme is: [arch]_lib[pluginName].zip.so
    private const val LATEST_RELEASE = "https://api.github.com/repos/bobbyesp/youtubedl-android/releases/latest"

    @Throws(Exception::class)
    suspend fun getRelease(releaseUrl: String = LATEST_RELEASE): Release? {
        return makeApiCall<Release?>(client, releaseUrl, null)
    }

    @Throws(Exception::class)
    fun getDownloadLinkForPlugin(architecture: CpuArchitecture, plugin: Plugin, release: Release): String {
        val pluginName = plugin.toString()
        val desiredArch = architecture.name.lowercase()
        val pluginFileName = "${desiredArch}_lib$pluginName.zip.so"
        val pluginAsset = release.assets.find { it.name.lowercase() == pluginFileName }

        return pluginAsset?.browser_download_url ?: throw Exception("Plugin $pluginName not found for architecture ${architecture.name}")
    }

    @Throws(Exception::class)
    suspend fun getDownloadLinkForPlugin(architecture: CpuArchitecture, plugin: Plugin): String {
        val release = getRelease() ?: throw Exception("No release found")
        return getDownloadLinkForPlugin(architecture, plugin, release)
    }
}