package com.yausername.youtubedl_android

import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.lang.Exception
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

internal class StreamProcessExtractor(
    private val buffer: StringBuffer,
    private val stream: InputStream,
    private val callback: ((Float, Long, String) -> Unit)?
) : Thread() {
    private val p = Pattern.compile("\\[download\\]\\s+(\\d+\\.\\d)% .* ETA (\\d+):(\\d+)")
    private val pAria2c =
        Pattern.compile("\\[#\\w{6}.*\\((\\d*\\.*\\d+)%\\).*?((\\d+)m)*((\\d+)s)*]")
    private var progress = PERCENT
    private var eta = ETA

    init {
        start()
    }

    override fun run() {
        var input: BufferedReader? = null
        try {
            input = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8))
            var currentLine: String?
            while (input.readLine().also { currentLine = it } != null) {
                if (callback != null) {
                    if (currentLine!!.startsWith("[")) processOutputLine(currentLine!!)
                }
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "failed to read stream", e)
        } finally {
            try {
                input?.close()
            } catch (e: IOException) {
                if (BuildConfig.DEBUG) Log.e(TAG, "failed to close stream", e)
            }
        }
    }


    private fun processOutputLine(line: String) {
        callback?.let { it(getProgress(line), getEta(line), line) }
    }

    private fun getProgress(line: String): Float {
        val matcher = p.matcher(line)
        if (matcher.find()) return matcher.group(GROUP_PERCENT).toFloat()
            .also { progress = it } else {
            val mAria2c = pAria2c.matcher(line)
            if (mAria2c.find()) return mAria2c.group(1).toFloat().also { progress = it }
        }
        return progress
    }

    private fun getEta(line: String): Long {
        val matcher = p.matcher(line)
        if (matcher.find()) return convertToSeconds(
            matcher.group(GROUP_MINUTES),
            matcher.group(GROUP_SECONDS)
        ).also { eta = it.toLong() }.toLong() else {
            val mAria2c = pAria2c.matcher(line)
            if (mAria2c.find()) return convertToSeconds(
                mAria2c.group(3),
                mAria2c.group(5)
            ).also { eta = it.toLong() }.toLong()
        }
        return eta
    }

    private fun convertToSeconds(minutes: String?, seconds: String?): Int {
        if (seconds == null) return 0 else if (minutes == null) return seconds.toInt()
        return minutes.toInt() * 60 + seconds.toInt()
    }

    companion object {
        private val TAG = StreamProcessExtractor::class.java.simpleName
        private const val ETA: Long = -1
        private const val PERCENT = -1.0f
        private const val GROUP_PERCENT = 1
        private const val GROUP_MINUTES = 2
        private const val GROUP_SECONDS = 3
    }
}