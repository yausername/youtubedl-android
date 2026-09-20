package com.yausername.youtubedl_android

import android.content.Context
import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import com.yausername.youtubedl_android.mapper.VideoInfo
import com.yausername.youtubedl_common.SharedPrefsHelper
import com.yausername.youtubedl_common.SharedPrefsHelper.update
import com.yausername.youtubedl_common.utils.ZipUtils.unzip
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.util.Collections
import kotlin.collections.set

@Suppress("ConstPropertyName")
object YoutubeDL {
    private var initialized = false
    private var pythonPath: File? = null
    private var ffmpegPath: File? = null
    private var aria2cPath: File? = null
    private var ytdlpPath: File? = null
    private var binDir: File? = null
    private var ENV_LD_LIBRARY_PATH: String? = null
    private var ENV_SSL_CERT_FILE: String? = null
    private var ENV_PYTHONHOME: String? = null
    private var TMPDIR: String = ""
    private val idProcessMap = Collections.synchronizedMap(HashMap<String, Process>())
    private val idPidMap = Collections.synchronizedMap(HashMap<String, Int>())
    private val cancellingProcessIds = Collections.synchronizedSet(HashSet<String>())

    @Synchronized
    @Throws(YoutubeDLException::class)
    fun init(appContext: Context) {
        if (initialized) return
        val baseDir = File(appContext.noBackupFilesDir, baseName)
        if (!baseDir.exists()) baseDir.mkdir()
        val packagesDir = File(baseDir, packagesRoot)
        binDir = File(appContext.applicationInfo.nativeLibraryDir)
        pythonPath = File(binDir, pythonBinName)
        ffmpegPath = File(binDir, ffmpegBinName)
        val pythonDir = File(packagesDir, pythonDirName)
        val ffmpegDir = File(packagesDir, ffmpegDirName)
        val aria2cDir = File(packagesDir, aria2cDirName)
        val ytdlpDir = File(baseDir, ytdlpDirName)
        ytdlpPath = File(ytdlpDir, ytdlpBin)
        ENV_LD_LIBRARY_PATH = pythonDir.absolutePath + "/usr/lib" + ":" +
        ffmpegDir.absolutePath + "/usr/lib" + ":" +
        aria2cDir.absolutePath + "/usr/lib" + ":" +
        binDir!!.absolutePath
        ENV_SSL_CERT_FILE = pythonDir.absolutePath + "/usr/lib/python3.14/site-packages/certifi/cacert.pem"
        ENV_PYTHONHOME = pythonDir.absolutePath + "/usr"
        TMPDIR = appContext.cacheDir.absolutePath
        initPython(appContext, pythonDir)
        initFFmpeg(appContext, ffmpegDir)
        initAria2c(appContext, aria2cDir)
        init_ytdlp(appContext, ytdlpDir)
        initialized = true
    }

    @Throws(YoutubeDLException::class)
    private fun initFFmpeg(appContext: Context, ffmpegDir: File) {
        val ffmpegLib = File(binDir, ffmpegLibName)
        val ffmpegSize = ffmpegLib.length().toString()

        if (!ffmpegDir.exists() || shouldUpdateFFmpeg(appContext, ffmpegSize)) {
            FileUtils.deleteQuietly(ffmpegDir)
            ffmpegDir.mkdirs()

            try {
                unzip(ffmpegLib, ffmpegDir)
            } catch (e: Exception) {
                FileUtils.deleteQuietly(ffmpegDir)
                throw YoutubeDLException("failed to initialize FFmpeg", e)
            }

            updateFFmpeg(appContext, ffmpegSize)
        }
    }

    private fun shouldUpdateFFmpeg(appContext: Context, version: String): Boolean {
        return version != SharedPrefsHelper[appContext, ffmpegLibVersion]
    }

    private fun updateFFmpeg(appContext: Context, version: String) {
        update(appContext, ffmpegLibVersion, version)
    }

    @Throws(YoutubeDLException::class)
    fun init_ytdlp(appContext: Context, ytdlpDir: File) {
        if (!ytdlpDir.exists()) ytdlpDir.mkdirs()
        val ytdlpBinary = File(ytdlpDir, ytdlpBin)
        if (!ytdlpBinary.exists()) {
            try {
                val inputStream =
                    appContext.resources.openRawResource(R.raw.ytdlp) /* will be renamed to yt-dlp */
                FileUtils.copyInputStreamToFile(inputStream, ytdlpBinary)
            } catch (e: Exception) {
                FileUtils.deleteQuietly(ytdlpDir)
                throw YoutubeDLException("failed to initialize", e)
            }
        }
    }

    @Throws(YoutubeDLException::class)
    fun initPython(appContext: Context, pythonDir: File) {
        val pythonLib = File(binDir, pythonLibName)
        // using size of lib as version
        val pythonSize = pythonLib.length().toString()

        if (!pythonDir.exists() || shouldUpdatePython(appContext, pythonSize)) {
            FileUtils.deleteQuietly(pythonDir)
            pythonDir.mkdirs()

            try {
                unzip(pythonLib, pythonDir)
            } catch (e: Exception) {
                FileUtils.deleteQuietly(pythonDir)
                throw YoutubeDLException("failed to initialize", e)
            }

            updatePython(appContext, pythonSize)
        }

        // Avoid shadowing Android's system liblzma.so through LD_LIBRARY_PATH.
        FileUtils.deleteQuietly(
            File(pythonDir, "usr/lib/liblzma.so")
        )
    }

    private fun shouldUpdatePython(appContext: Context, version: String): Boolean {
        return version != SharedPrefsHelper[appContext, pythonLibVersion]
    }

    private fun updatePython(appContext: Context, version: String) {
        update(appContext, pythonLibVersion, version)
    }

    @Throws(YoutubeDLException::class)
    fun initAria2c(appContext: Context, aria2cDir: File) {
        val aria2cLib = File(binDir, aria2cLibName)
        val aria2cSize = aria2cLib.length().toString()

        if (!aria2cDir.exists() || shouldUpdateAria2c(appContext, aria2cSize)) {
            FileUtils.deleteQuietly(aria2cDir)
            aria2cDir.mkdirs()
            try {
                unzip(aria2cLib, aria2cDir)
            } catch (e: Exception) {
                FileUtils.deleteQuietly(aria2cDir)
                throw YoutubeDLException("failed to initialize aria2c", e)
            }
            updateAria2c(appContext, aria2cSize)
        }

        aria2cPath = File(aria2cDir, "usr/bin/aria2c")
        val aria2cBinary = aria2cPath!!

        if (!aria2cBinary.exists()) {
            throw YoutubeDLException(
                "aria2c binary not found at ${aria2cBinary.absolutePath}"
            )
        }

        if (!aria2cBinary.setExecutable(true)) {
            throw YoutubeDLException(
                "failed to make aria2c executable at ${aria2cBinary.absolutePath}"
            )
        }
    }

    private fun shouldUpdateAria2c(appContext: Context, version: String): Boolean {
        return version != SharedPrefsHelper[appContext, aria2cLibVersion]
    }

    private fun updateAria2c(appContext: Context, version: String) {
        update(appContext, aria2cLibVersion, version)
    }

    private fun assertInit() {
        check(initialized) { "instance not initialized" }
    }


    @Throws(YoutubeDLException::class, InterruptedException::class, CanceledException::class)
    fun getInfo(url: String): VideoInfo {
        val request = YoutubeDLRequest(url)
        return getInfo(request)
    }


    @Throws(YoutubeDLException::class, InterruptedException::class, CanceledException::class)
    fun getInfo(request: YoutubeDLRequest): VideoInfo {
        request.addOption("--dump-json")
        val response = execute(request, null, null)
        val videoInfo: VideoInfo = try {
            objectMapper.readValue(response.out, VideoInfo::class.java)
        } catch (e: IOException) {
            throw YoutubeDLException("Unable to parse video information", e)
        } ?: throw YoutubeDLException("Failed to fetch video information")
        return videoInfo
    }

    private fun ignoreErrors(request: YoutubeDLRequest, out: String): Boolean {
        return request.hasOption("--dump-json") && !out.isEmpty() && request.hasOption("--ignore-errors")
    }

    private fun waitForIdRelease(
        processId: String,
        timeout: Long = 2000L,
        confirmation: Long = 50L
    ): Boolean {

        val deadline = System.currentTimeMillis() + timeout

        while (System.currentTimeMillis() < deadline) {
            if (!idProcessMap.containsKey(processId)) {
                return true
        }
            Thread.sleep(confirmation)
        }
        return !idProcessMap.containsKey(processId)
    }


    private fun lingeringProcessTermination(
        processId: String?,
        process: Process
    ) {

        val pid = processId?.let { idPidMap[it] }

        if (pid != null) {
            destroyChildProcesses(pid)
        }

        process.destroy()

        val deadline = System.currentTimeMillis() + 2000L

        while (System.currentTimeMillis() < deadline) {
            try {
                 process.exitValue()
                return
            } catch (_: IllegalThreadStateException) {
                Thread.sleep(50)
            }
        }

        if (pid != null) {
            destroyChildProcesses(pid)

            try {
                ProcessBuilder(
                    "/system/bin/kill",
                    "-KILL",
                    pid.toString()
                ).start().waitFor()
            } catch (e: Exception) {
                Log.e("YoutubeDL", "Failed to kill lingering process $pid", e)
            }
        }
    }

    private fun readPidFile(pidFile: File): Int? {
        repeat(50) {
            val pid = if (pidFile.exists()) {
                pidFile.readText().trim().toIntOrNull()
            } else {
                null
            }

            if (pid != null) {
                return pid
            }

            Thread.sleep(50)
        }

        return null
    }


    fun destroyProcessById(id: String): Boolean {
        val process = idProcessMap[id] ?: return false
        Log.d("YoutubeDLRace", "cancel requested id=$id")

        cancellingProcessIds.add(id)

        return try {
            lingeringProcessTermination(id, process)
            true
        } finally {
            idProcessMap.remove(id)
            idPidMap.remove(id)
        }
    }

    private fun destroyChildProcesses(pid: Int): Boolean {
        return try {
            val command = """
            kill_children() {
                for child in ${'$'}(cat /proc/${'$'}1/task/${'$'}1/children 2>/dev/null); do
                    kill_children "${'$'}child"
                    kill -TERM "${'$'}child" 2>/dev/null
                done
            }

            kill_children $pid
        """.trimIndent()

            ProcessBuilder(
                "/system/bin/sh",
                "-c",
                command
            ).start().waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    class CanceledException : Exception()


    @Throws(YoutubeDLException::class, InterruptedException::class, CanceledException::class)
    fun execute(
        request: YoutubeDLRequest,
        processId: String? = null,
        callback: ((Float, Long, String) -> Unit)? = null
    ): YoutubeDLResponse {
        return executeImpl(request, processId, false, callback)
    }


    @JvmOverloads
    @Throws(YoutubeDLException::class, InterruptedException::class, CanceledException::class)
    fun execute(
        request: YoutubeDLRequest,
        processId: String? = null,
        redirectErrorStream: Boolean = false,
        callback: ((Float, Long, String) -> Unit)? = null
    ): YoutubeDLResponse {
        return executeImpl(request, processId, redirectErrorStream, callback)
    }


    @Throws(YoutubeDLException::class, InterruptedException::class, CanceledException::class)
    private fun executeImpl(
        request: YoutubeDLRequest,
        processId: String? = null,
        redirectErrorStream: Boolean = false,
        callback: ((Float, Long, String) -> Unit)? = null
    ) : YoutubeDLResponse {
        assertInit()
        Log.d("YoutubeDLRace", "execute start id=$processId")

        if (processId != null && idProcessMap.containsKey(processId)) {
            Log.d("YoutubeDLRace", "id occupied id=$processId")
            if (!waitForIdRelease(processId)) {
                Log.e("YoutubeDLRace", "Process ID already exists id=$processId")
                throw YoutubeDLException("Process ID already exists")
            }
            Log.d("YoutubeDLRace", "id released after wait id=$processId")
        }
        // disable caching unless explicitly requested
        if (!request.hasOption("--cache-dir") || request.getOption("--cache-dir") == null) {
            request.addOption("--no-cache-dir")
        }

        // This automatically makes Quick JS available at runtime without outside invocation,
        // it's commented out but can be uncommented if you want to make it work off-rip on runtime.

//        val quickJsCli = File(
//            ffmpegPath!!.parentFile,
//            "libqjs-cli.so"
//        )
//
//        if (quickJsCli.exists()) {
//            request.addOption(
//                "--js-runtimes",
//                "quickjs:${quickJsCli.absolutePath}"
//            )
//        }

        // For curl_cffi and cffi, have something like "request.addOption("--impersonate", "chrome")".
        // I will try to squash bugs as much as I can, I also suggest having this feature as "Cutting-Edge"
        // or "Experimental" and ask users to report bugs, so it doesn't mess with anything working currently.
        // As I stated in the PR, This feature is almost guaranteed to either not work or need extra work on x86.

        if (request.buildCommand().contains("libaria2c.so")) {
            request
                .addOption("--external-downloader-args", "aria2c:--summary-interval=1")
                .addOption(
                    "--external-downloader-args",
                    "aria2c:--ca-certificate=$ENV_SSL_CERT_FILE"
                )
        }

        /* Set ffmpeg location, See https://github.com/xibr/ytdlp-lazy/issues/1 */
        request.addOption("--ffmpeg-location", ffmpegPath!!.absolutePath)
        val process: Process
        val exitCode: Int
        val outBuffer = StringBuffer() //stdout
        val errBuffer = StringBuffer() //stderr
        val startTime = System.currentTimeMillis()
        val args = request.buildCommand().map { arg ->
            if (arg == "libaria2c.so") {
                aria2cPath!!.absolutePath
            } else {
                arg
            }
        }

        val pidFile = processId?.let {
            File(TMPDIR, "ytdlp-$it.pid").apply {
                FileUtils.deleteQuietly(this)
            }
        }
        val command: MutableList<String?> = ArrayList()

        if (pidFile != null) {
            val shellCommand = """
        echo ${'$'}${'$'} > "${pidFile.absolutePath}"
        exec "${'$'}@"
    """.trimIndent()

            command.addAll(
                listOf(
                    "/system/bin/sh",
                    "-c",
                    shellCommand,
                    "sh",
                    pythonPath!!.absolutePath,
                    ytdlpPath!!.absolutePath
                )
            )
        } else {
            command.addAll(
                listOf(
                    pythonPath!!.absolutePath,
                    ytdlpPath!!.absolutePath
                )
            )
        }

        command.addAll(args)
        val processBuilder = ProcessBuilder(command)
            .redirectErrorStream(redirectErrorStream)

        processBuilder.environment().apply {
            this["LD_LIBRARY_PATH"] = ENV_LD_LIBRARY_PATH
            this["SSL_CERT_FILE"] = ENV_SSL_CERT_FILE
            this["PATH"] =
                System.getenv("PATH") + ":" + binDir!!.absolutePath + ":" + aria2cPath!!.parentFile!!.absolutePath
            this["PYTHONHOME"] = ENV_PYTHONHOME
            this["OPENSSL_MODULES"] = "$ENV_PYTHONHOME/lib/ossl-modules"
            this["HOME"] = ENV_PYTHONHOME
            this["TMPDIR"] = TMPDIR
        }

        process = try {
            processBuilder.start()
        } catch (e: IOException) {
            throw YoutubeDLException(e)
        }

        if (processId != null) {
            idProcessMap[processId] = process
            Log.d("YoutubeDLRace", "registered id=$processId")

            pidFile?.let {
                readPidFile(it)?.let { pid ->
                    idPidMap[processId] = pid
                }
                FileUtils.deleteQuietly(it)
            }
        }

        try {
            val outStream = process.inputStream
            val errStream = process.errorStream
            val stdOutProcessor = StreamProcessExtractor(outBuffer, outStream, callback)
            val stdErrProcessor = StreamGobbler(errBuffer, errStream)

            exitCode = try {
                stdOutProcessor.join()
                stdErrProcessor.join()
                process.waitFor()
            } catch (e: InterruptedException) {
                lingeringProcessTermination(processId, process)
                throw e
            }

            val out = outBuffer.toString()
            val err = errBuffer.toString()

            if (exitCode > 0) {

                if (processId != null &&
                    (cancellingProcessIds.contains(processId) || !idProcessMap.containsKey(processId))) {
                    throw CanceledException()
                }

                if (!ignoreErrors(request, out)) {
                    throw YoutubeDLException(err)
                }
            }
            val elapsedTime = System.currentTimeMillis() - startTime

            val youtubeDLResponse = YoutubeDLResponse(command, exitCode, elapsedTime, out, err)
            return youtubeDLResponse
        } finally {
            if (processId != null) {
                idProcessMap.remove(processId)
                idPidMap.remove(processId)
                cancellingProcessIds.remove(processId)
                Log.d("YoutubeDLRace", "cleanup id=$processId")
            }
        }
    }

    @Synchronized
    @Throws(YoutubeDLException::class)
    fun updateYoutubeDL(
        appContext: Context,
        updateChannel: UpdateChannel = UpdateChannel.STABLE
    ): UpdateStatus? {
        assertInit()
        return try {
            YoutubeDLUpdater.update(appContext, updateChannel)
        } catch (e: IOException) {
            throw YoutubeDLException("failed to update youtube-dl", e)
        }
    }

    fun version(appContext: Context?): String? {
        return YoutubeDLUpdater.version(appContext)
    }

    fun versionName(appContext: Context?): String? {
        return YoutubeDLUpdater.versionName(appContext)
    }

    enum class UpdateStatus {
        DONE, ALREADY_UP_TO_DATE
    }

    open class UpdateChannel(val apiUrl: String) {
        object STABLE : UpdateChannel("https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest")
        object NIGHTLY :
            UpdateChannel("https://api.github.com/repos/yt-dlp/yt-dlp-nightly-builds/releases/latest")
        object MASTER :
            UpdateChannel("https://api.github.com/repos/yt-dlp/yt-dlp-master-builds/releases/latest")

        companion object {
            @JvmField
            val _STABLE: STABLE = STABLE

            @JvmField
            val _NIGHTLY: NIGHTLY = NIGHTLY

            @JvmField
            val _MASTER: MASTER = MASTER
        }
    }


    const val baseName = "youtubedl-android"
    private const val packagesRoot = "packages"
    private const val pythonBinName = "libpython.so"
    private const val pythonLibName = "libpython.zip.so"
    private const val pythonDirName = "python"
    private const val ffmpegDirName = "ffmpeg"
    private const val ffmpegBinName = "libffmpeg.so"
    private const val ffmpegLibName = "libffmpeg.zip.so"
    private const val ffmpegLibVersion = "ffmpegLibVersion"
    private const val aria2cDirName = "aria2c"
    private const val aria2cLibName = "libaria2.zip.so"
    private const val aria2cLibVersion = "aria2cLibVersion"
    const val ytdlpDirName = "yt-dlp"
    const val ytdlpBin = "yt-dlp"
    private const val pythonLibVersion = "pythonLibVersion"
    val objectMapper = ObjectMapper()

    @JvmStatic
    fun getInstance() = this
}
