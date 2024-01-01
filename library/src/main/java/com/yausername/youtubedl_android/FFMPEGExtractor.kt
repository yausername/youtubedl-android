package com.yausername.youtubedl_android

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class FFMPEGExtractor(
            private val process: Process,
            private val progressCallback:(line:String?)->Unit,
            private val onComplete:(line:String)->Unit
) {
    fun start(){
        ProgressCallback(process,progressCallback)
            .start()
        CompleteCallback(process,onComplete)
            .start()
    }
    class ProgressCallback(private val process:Process,
                           private val progressCallback:(line:String?)->Unit):Thread(){
        override fun run() {
            val pythonPID = ProcessUtils.getPythonProcessId(process)
            while(true){
                val ffmpegPid = ProcessUtils.getFFMPEGProcessId(pythonPID)
                val progressFilePath = "/proc/$ffmpegPid/fd/2"
                val progressfile = File(progressFilePath)
                if (progressfile.exists()) {
                    val inputStream = FileInputStream(progressfile)
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        if(line?.contains("size") == true){
                            progressCallback(line)
                        }
                    }
                }

                sleep(1000)
            }
        }
    }
    class CompleteCallback(private val process:Process,
                           private val onComplete:(line:String)->Unit):Thread(){
        override fun run() {
            val pythonPID = ProcessUtils.getPythonProcessId(process)
            while(true){
                val ffmpegPid = ProcessUtils.getFFMPEGProcessId(pythonPID)
                val completeFilePath = "/proc/$ffmpegPid/fd/1"
                val completefile = File(completeFilePath)
                if (completefile.exists()) {
                    val inputStream = FileInputStream(completefile)
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        line?.let{
                            onComplete(it)
                        }
                    }
                }

                sleep(1000)
            }
        }
    }
}