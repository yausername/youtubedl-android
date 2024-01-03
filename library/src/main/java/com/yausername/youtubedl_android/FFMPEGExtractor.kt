package com.yausername.youtubedl_android

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

class FFMPEGExtractor{
    private val progressCallbacks = ConcurrentHashMap<String,ProgressThread>()
    fun start(id:String,process: Process,progressCallback:((size:Int?,line:String?)->Unit)? = null){
        if(!progressCallbacks.containsKey(id)){
            val callback = ProgressThread(process,progressCallback)
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
    inner class ProgressThread(private val process:Process,
                               private val progressCallback:((size:Int?,line:String?)->Unit)?=null
    ):Thread(){
        var shouldContinue = true
        override fun run() {
            val pythonPID = ProcessUtils.getPythonProcessId(process)
            while(shouldContinue){
                val ffmpegPid = ProcessUtils.getFFMPEGProcessId(pythonPID)
                val progressFilePath = "/proc/$ffmpegPid/fd/2"
                val progressfile = File(progressFilePath)
                if (progressfile.exists()) {
                    val inputStream = FileInputStream(progressfile)
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val size = ProcessUtils.extractSize(line)
                        progressCallback?.let { it(size,line) }
                    }
                }

                sleep(1000)
            }
        }

        fun stopNow(){
            shouldContinue = false
        }
    }
}