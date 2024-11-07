package com.yausername.youtubedl_android

import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

class FFMPEGExtractor{
    private val progressCallbacks = ConcurrentHashMap<String,ProgressThread>()
    fun start(id:String, process: Process,
              ffmpegcallback:((size:Int?, line:String?)->Unit)? = null){
        if(!progressCallbacks.containsKey(id)){
            val callback = ProgressThread(process,ffmpegcallback)
            progressCallbacks[id] = callback
            callback.start()
        }
    }
    fun stop(id:String){
        if(progressCallbacks.containsKey(id)){
            progressCallbacks[id]?.stopNow()
            progressCallbacks.remove(id)
        }
    }
    class ProgressThread(
        private val process: Process,
        private val ffmpegcallback: ((size: Int?, line: String?) -> Unit)? = null
    ) : Thread() {

        private var shouldContinue = true
        private var mFfmpegPid = 0

        override fun run() {
            try {
                val pythonPID = ProcessUtils.getPythonProcessId(process)
                var line: String?
                //thread { monitorInternetConnection() }

                while (shouldContinue) {
                    mFfmpegPid = ProcessUtils.getFFMPEGProcessId(pythonPID)
                    val progressFilePath = "/proc/$mFfmpegPid/fd/2"
                    val progressfile = File(progressFilePath)

                    if (progressfile.exists()) {
                        val inputStream = FileInputStream(progressfile)
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        while (reader.readLine().also { line = it } != null) {
                            val size = ProcessUtils.extractSize(line)
                            ffmpegcallback?.let { it(size,line) }
                        }
                    }
                    sleep(1000)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                // Handle the exception as needed
            }
        }

        fun stopNow() {
            ProcessUtils.killProcess(mFfmpegPid)
            process.destroy()
            shouldContinue = false
        }

        private fun monitorInternetConnection() {
            try {
                while (shouldContinue) {
                    if (!isInternetReachable()) {
                        stopNow()
                    } 
                    sleep(5000)
                }
            } catch (ex: Exception) {
                Log.e("FFMPEGExtractor", "Exception in monitorInternetConnection", ex)
            }
        }


        private fun isInternetReachable(): Boolean {
            return try {
                val process = ProcessBuilder("ping", "-c", "1","-W","3", "google.com").start()
                val exitCode = process.waitFor()
                exitCode == 0
            } catch (e: IOException) {
                false
            } catch (e: InterruptedException) {
                false
            }
        }
    }
}