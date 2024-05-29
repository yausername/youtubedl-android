package com.yausername.youtubedl_common.data.remote

import android.util.Log
import com.yausername.youtubedl_common.utils.exceptions.FileDownloadException
import com.yausername.youtubedl_common.utils.network.Ktor.client
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.prepareGet
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import java.io.File

object FileDownloader {
    @Throws(FileDownloadException::class)
    suspend fun downloadFileWithProgress(
        fileUrl: String,
        localFile: File,
        overwrite: Boolean = false,
        progressCallback: (progress: Int) -> Unit
    ) {
        if(localFile.exists() && overwrite) {
            Log.i("File Downloader","Deleting existing file for overwrite: ${localFile.absolutePath}")
            localFile.delete()
        }

        try {
            client.prepareGet(fileUrl).execute { response ->
                val bytesChannel: ByteReadChannel = response.body()
                val contentLength = response.contentLength() ?: -1
                var downloadedBytes = 0L

                while (!bytesChannel.isClosedForRead) {
                    val receivedPacket = bytesChannel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                    while (!receivedPacket.isEmpty) {
                        val bytes = receivedPacket.readBytes()
                        localFile.appendBytes(bytes)
                        downloadedBytes += bytes.size

                        // Calculate progress
                        val progress = if (contentLength.toInt() != -1) {
                            ((downloadedBytes.toDouble() / contentLength) * 100).toInt()
                        } else {
                            // If content length is unknown, report progress as -1
                            -1
                        }

                        // Invoke the progress callback
                        progressCallback(progress)

                    }
                }
                Log.i("File Downloader","Downloaded file from $fileUrl || Content length: $contentLength")
            }
        } catch (e: ResponseException) {
            handleResponseException(e)
        } catch (e: Exception) {
            Log.e("File Downloader","An error occurred during file download", e)
            throw FileDownloadException("File download failed", e)
        }
    }


    private fun handleResponseException(e: ResponseException) {
        val statusCode = e.response.status.value
        when(statusCode) {
            403 -> throw FileDownloadException("403 Forbidden")
            404 -> throw FileDownloadException("404 Not Found")
            429 -> throw FileDownloadException("429 Too Many Requests")
            else -> throw FileDownloadException("Unknown error", e)
        }
    }
}