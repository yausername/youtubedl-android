package com.yausername.youtubedl_android

import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

class FFMPEGExtractor{
    private val progressCallbacks = ConcurrentHashMap<String,ProgressThread>()
    fun start(id:String,process: Process,progressCallback:((size:Int?,line:String?,processavailable:Boolean)->Unit)? = null){
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
                               private val progressCallback:((size:Int?,line:String?,processavailable:Boolean)->Unit)?=null
    ):Thread(){
        var shouldContinue = true
        override fun run() {
           try{
               val pythonPID = ProcessUtils.getPythonProcessId(process)
               var line: String?
               while(shouldContinue){
                   val ffmpegPid = ProcessUtils.getFFMPEGProcessId(pythonPID)
                   val progressFilePath = "/proc/$ffmpegPid/fd/2"
                   val progressfile = File(progressFilePath)

                   if (progressfile.exists()) {
                       val inputStream = FileInputStream(progressfile)
                       val reader = BufferedReader(InputStreamReader(inputStream))
                       while (reader.readLine().also { line = it } != null) {
                           val size = ProcessUtils.extractSize(line)
                           Log.e(TAG,"Line Internal: ${line}")
                           progressCallback?.let { it(size,line,true) }
                       }
                       progressCallback?.let { it(-1,line,true) }
                   }
                   sleep(100)
               }
           }catch (ex:Exception){
               throw IOException()
           }
        }

        fun stopNow(){
            shouldContinue = false
        }
    }
}