package com.yausername.youtubedl_common.utils

import android.content.Context
import android.os.Build
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileNotFoundException

data class PackagedArchive(
    val file: File,
    val version: String,
)

object PackagedArchiveLocator {
    private const val assetRoot = "youtubedl-android"
    private const val assetCacheDirName = "asset-archives"
    private const val versionFileSuffix = ".version"

    fun resolve(
        appContext: Context,
        baseDir: File,
        fileName: String,
    ): PackagedArchive? {
        val abi = Build.SUPPORTED_ABIS.firstOrNull()
        if (abi != null) {
            val assetPath = "$assetRoot/$abi/$fileName"
            val payloadVersion = appContext.packageManager.getPackageInfo(appContext.packageName, 0).lastUpdateTime.toString()

            copyAssetArchiveIfPresent(
                appContext = appContext,
                baseDir = baseDir,
                fileName = fileName,
                assetPath = assetPath,
                payloadVersion = payloadVersion,
            )?.let { cachedArchive ->
                return PackagedArchive(
                    file = cachedArchive,
                    version = "asset:$payloadVersion:${cachedArchive.length()}",
                )
            }
        }

        val nativeArchive = File(appContext.applicationInfo.nativeLibraryDir, fileName)
        if (!nativeArchive.exists()) {
            return null
        }

        return PackagedArchive(
            file = nativeArchive,
            version = "native:${nativeArchive.length()}",
        )
    }

    private fun copyAssetArchiveIfPresent(
        appContext: Context,
        baseDir: File,
        fileName: String,
        assetPath: String,
        payloadVersion: String,
    ): File? {
        val archiveDir = File(baseDir, "$assetCacheDirName/${Build.SUPPORTED_ABIS.firstOrNull() ?: return null}")
        val archiveFile = File(archiveDir, fileName)
        val versionFile = File(archiveDir, "$fileName$versionFileSuffix")

        return try {
            appContext.assets.open(assetPath).use { assetStream ->
                archiveDir.mkdirs()
                val cachedVersion = versionFile.takeIf(File::exists)?.readText()
                if (!archiveFile.exists() || cachedVersion != payloadVersion) {
                    FileUtils.copyInputStreamToFile(assetStream, archiveFile)
                    versionFile.writeText(payloadVersion)
                }
                archiveFile
            }
        } catch (_: FileNotFoundException) {
            null
        }
    }
}
