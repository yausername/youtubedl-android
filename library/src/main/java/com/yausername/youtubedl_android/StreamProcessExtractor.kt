package com.yausername.youtubedl_android

import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
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
        try {
            val input: Reader = InputStreamReader(stream, StandardCharsets.UTF_8)
            val currentLine = StringBuilder()
            val maxLineLength = 10_000  // Limit to 10,000 characters per line
            val maxBufferLength = 1_000_000  // Limit to 1 million characters in total buffer

            var nextChar: Int
            while (input.read().also { nextChar = it } != -1) {

                val c = nextChar.toChar()

                // Safely append to buffer
                synchronized(buffer) {
                    buffer.append(c)
                    if (buffer.length > maxBufferLength) {
                        buffer.delete(0, buffer.length / 2) // truncate older half
                    }
                }

                // Process line if newline character is detected
                if (c == '\r' || c == '\n') {
                    if (callback != null) {
                        val line = currentLine.toString()
                        processOutputLine(line)
                    }
                    currentLine.setLength(0)
                } else {
                    // Protect against excessively long lines
                    if (currentLine.length < maxLineLength) {
                        currentLine.append(c)
                    } else {
                        // Drop the line if it's too long (could also log or throw)
                        currentLine.setLength(0)
                    }
                }
            }
        } catch (e: IOException) {
            if (BuildConfig.DEBUG) Log.e(TAG, "failed to read stream", e)
        } finally {
            try {
                stream.close()
            } catch (ignored: IOException) {
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