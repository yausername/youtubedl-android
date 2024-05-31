package com.yausername.youtubedl_android

import android.content.Context
import android.os.Build
import com.yausername.youtubedl_android.YoutubeDL.assertInit
import com.yausername.youtubedl_android.data.local.streams.StreamGobbler
import com.yausername.youtubedl_android.data.local.streams.StreamProcessExtractor
import com.yausername.youtubedl_android.data.remote.YoutubeDLUpdater
import com.yausername.youtubedl_android.domain.UpdateChannel
import com.yausername.youtubedl_android.domain.UpdateStatus
import com.yausername.youtubedl_android.domain.model.YoutubeDLResponse
import com.yausername.youtubedl_android.domain.model.videos.VideoInfo
import com.yausername.youtubedl_android.util.exceptions.YoutubeDLException
import com.yausername.youtubedl_common.Constants
import com.yausername.youtubedl_common.Constants.LIBRARY_NAME
import com.yausername.youtubedl_common.Constants.PACKAGES_ROOT_NAME
import com.yausername.youtubedl_common.SharedPrefsHelper
import com.yausername.youtubedl_common.SharedPrefsHelper.update
import com.yausername.youtubedl_common.domain.Dependency
import com.yausername.youtubedl_common.domain.model.DownloadedDependencies
import com.yausername.youtubedl_common.utils.dependencies.DependenciesUtil
import com.yausername.youtubedl_common.utils.dependencies.dependencyDownloadCallback
import com.yausername.youtubedl_common.utils.files.FilesUtil.createDirectoryIfNotExists
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.util.Collections

abstract class YoutubeDLCore {
    private var initialized = false
    protected lateinit var binariesDirectory: File

    private var pythonPath: File? = null
    private var ffmpegPath: File? = null
    private lateinit var ytdlpPath: File

    /* ENVIRONMENT VARIABLES */
    private lateinit var ENV_LD_LIBRARY_PATH: String
    private lateinit var ENV_SSL_CERT_FILE: String
    private lateinit var ENV_PYTHONHOME: String

    //Map of process id associated with the process
    protected open val idProcessMap: MutableMap<String, Process> =
        Collections.synchronizedMap(HashMap<String, Process>())

    /**
     * Initializes the library. This method should be called before any other method.
     * @param appContext the application context
     */
    @Synchronized
    @Throws(YoutubeDLException::class, IllegalStateException::class)
    open fun init(appContext: Context) {
        if (initialized) return

        val baseDir = File(appContext.noBackupFilesDir, LIBRARY_NAME)

        createDirectoryIfNotExists(baseDir)

        val packagesDir = File(baseDir, PACKAGES_ROOT_NAME)

        binariesDirectory =
            File(appContext.applicationInfo.nativeLibraryDir) //Here are all the binaries provided by the jniLibs folder

        pythonPath = File(binariesDirectory, Constants.BinariesName.PYTHON)
        ffmpegPath = File(binariesDirectory, Constants.BinariesName.FFMPEG)

        // Create dependencies packages directory (where they are extracted)
        val pythonDir = File(
            packagesDir, Constants.DirectoriesName.PYTHON
        )
        val ffmpegDir = File(
            packagesDir, Constants.DirectoriesName.FFMPEG
        )
        val aria2cDir = File(packagesDir, Constants.DirectoriesName.ARIA2C)

        val ytdlpDir = File(
            baseDir, Constants.DirectoriesName.YTDLP
        )
        ytdlpPath = File(ytdlpDir, Constants.BinariesName.YTDLP)

        // Set environment variables
        ENV_LD_LIBRARY_PATH =
            pythonDir.absolutePath + "/usr/lib" + ":" + ffmpegDir.absolutePath + "/usr/lib" + ":" + aria2cDir.absolutePath + "/usr/lib"
        ENV_SSL_CERT_FILE = pythonDir.absolutePath + "/usr/etc/tls/cert.pem"
        ENV_PYTHONHOME = pythonDir.absolutePath + "/usr"

        initPython(appContext, pythonDir)
        initYtdlp(appContext, ytdlpDir)

        initialized = true
    }

    @JvmName("ensureDependenciesBridge")
    @Throws(IllegalStateException::class)
    fun ensureDependencies(
        appContext: Context,
        skipDependencies: List<Dependency> = emptyList(),
        callback: dependencyDownloadCallback? = null
    ): DownloadedDependencies =
        DependenciesUtil.ensureDependencies(appContext, skipDependencies, callback)

    abstract fun initPython(appContext: Context, pythonDir: File)

    abstract fun initYtdlp(appContext: Context, ytdlpDir: File)

    @Throws(YoutubeDLException::class, InterruptedException::class, CanceledException::class)
    fun getInfo(url: String): VideoInfo {
        val request = YoutubeDLRequest(url)
        return getInfo(request)
    }

    /**
     * Gets video information
     * @param request the request object
     * @return the video information
     */
    @Throws(YoutubeDLException::class, InterruptedException::class, CanceledException::class)
    fun getInfo(request: YoutubeDLRequest): VideoInfo {
        request.addOption("--dump-json")
        val response = execute(request, null, null)
        val videoInfo: VideoInfo = try {
            json.decodeFromString<VideoInfo>(response.out)
        } catch (e: IOException) {
            throw YoutubeDLException("Unable to parse video information", e)
        }
        return videoInfo
    }

    /**
     * Checks if the output errors should be ignored
     * @param request the request object
     * @param out the output
     * @return true if the errors should be ignored, false otherwise
     */
    private fun ignoreErrors(request: YoutubeDLRequest, out: String): Boolean {
        return request.hasOption("--dump-json") && out.isNotEmpty() && request.hasOption("--ignore-errors")
    }

    /**
     * Checks if a process with the given id is running and destroys it
     * @param id the process id
     * @return true if the process was destroyed successfully, false otherwise
     */
    fun destroyProcessById(id: String): Boolean {
        if (idProcessMap.containsKey(id)) {
            val p = idProcessMap[id]
            var alive = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                alive = p!!.isAlive
            }
            if (alive) {
                p!!.destroy()
                idProcessMap.remove(id)
                return true
            }
        }
        return false
    }


    class CanceledException : Exception()

    /**
     * Executes a request to yt-dlp
     * @param request the request object
     * @param processId the process id
     * @param callback a callback that will be called with the progress percentage, the ETA and the output
     * @return the response object
     */
    @JvmOverloads
    @Throws(YoutubeDLException::class, InterruptedException::class, CanceledException::class)
    fun execute(
        request: YoutubeDLRequest,
        processId: String? = null,
        callback: ((Float, Long, String) -> Unit)? = null
    ): YoutubeDLResponse {
        assertInit()
        if (processId != null && idProcessMap.containsKey(processId)) throw YoutubeDLException("Process ID already exists")
        // disable caching unless explicitly requested
        if (!request.hasOption("--cache-dir") || request.getOption("--cache-dir") == null) {
            request.addOption("--no-cache-dir")
        }

        /* Set ffmpeg location, See https://github.com/xibr/ytdlp-lazy/issues/1 */
        request.addOption("--ffmpeg-location", ffmpegPath!!.absolutePath)
        val youtubeDLResponse: YoutubeDLResponse
        val process: Process
        val exitCode: Int
        val outBuffer = StringBuilder() //stdout
        val errBuffer = StringBuilder() //stderr
        val startTime = System.currentTimeMillis()
        val args = request.buildCommand()
        val command: MutableList<String?> = ArrayList()
        command.addAll(listOf(pythonPath!!.absolutePath, ytdlpPath.absolutePath))
        command.addAll(args)
        val processBuilder = ProcessBuilder(command)
        processBuilder.environment().apply {
            this["LD_LIBRARY_PATH"] = ENV_LD_LIBRARY_PATH
            this["SSL_CERT_FILE"] = ENV_SSL_CERT_FILE
            this["PATH"] = System.getenv("PATH")!! + ":" + binariesDirectory.absolutePath
            this["PYTHONHOME"] = ENV_PYTHONHOME
            this["HOME"] = ENV_PYTHONHOME
        }

        process = try {
            processBuilder.start()
        } catch (e: IOException) {
            throw YoutubeDLException(e)
        }
        if (processId != null) {
            idProcessMap[processId] = process
        }
        val outStream = process.inputStream
        val errStream = process.errorStream
        val stdOutProcessor = StreamProcessExtractor(outBuffer, outStream, callback)
        val stdErrProcessor = StreamGobbler(errBuffer, errStream)
        exitCode = try {
            stdOutProcessor.join()
            stdErrProcessor.join()
            process.waitFor()
        } catch (e: InterruptedException) {
            process.destroy()
            if (processId != null) idProcessMap.remove(processId)
            throw e
        }
        val out = outBuffer.toString()
        val err = errBuffer.toString()
        if (exitCode > 0) {
            if (processId != null && !idProcessMap.containsKey(processId)) throw CanceledException()
            if (!ignoreErrors(request, out)) {
                idProcessMap.remove(processId)
                throw YoutubeDLException(err)
            }
        }
        idProcessMap.remove(processId)

        val elapsedTime = System.currentTimeMillis() - startTime
        youtubeDLResponse = YoutubeDLResponse(command, exitCode, elapsedTime, out, err)
        return youtubeDLResponse
    }

    /**
     * Asserts that the library is initialized
     */
    fun assertInit() {
        check(initialized) { "The library instance that you are trying to access is not initialized; please, check if you have initialized it by using the YoutubeDL.init() function" }
    }


    /**
     * Check if Python should be updated by using the zip file size (both new and old)
     * @param appContext the application context
     * @param version the current version of Python (the size of the zip file)
     */
    fun shouldUpdatePython(appContext: Context, version: String): Boolean {
        return version != SharedPrefsHelper[appContext, PYTHON_LIB_VERSION]
    }

    /**
     * Updates the Python version
     * @param appContext the application context
     * @param version the new version of Python (the size of the zip file)
     */
    fun updatePython(appContext: Context, version: String) {
        update(appContext, PYTHON_LIB_VERSION, version)
    }

    companion object {
        /**
         * Updates yt-dlp
         * @param appContext the application context
         * @param updateChannel the update channel
         * @return the update status
         */
        @Synchronized
        @Throws(YoutubeDLException::class)
        fun updateYoutubeDL(
            appContext: Context, updateChannel: UpdateChannel = UpdateChannel.STABLE
        ): UpdateStatus? {
            return try {
                assertInit()
                YoutubeDLUpdater.update(appContext, updateChannel)
            } catch (e: IOException) {
                throw YoutubeDLException("Failed to update yt-dlp!", e)
            }
        }

        /**
         * Gets the version of yt-dlp
         */
        fun version(appContext: Context?): String? {
            return YoutubeDLUpdater.version(appContext)
        }

        /**
         * Gets the version name of yt-dlp
         */
        fun versionName(appContext: Context?): String? {
            return YoutubeDLUpdater.versionName(appContext)
        }

        const val PYTHON_LIB_VERSION = "pythonLibVersion"

        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            coerceInputValues = true
        }
    }
}