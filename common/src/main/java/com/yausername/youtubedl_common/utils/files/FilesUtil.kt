package com.yausername.youtubedl_common.utils.files

import android.util.Log
import java.io.File

object FilesUtil {
    /**
     * Asserts that a directory exists and creates it if it doesn't
     * @param file the folder (File in Java) to check if it exists or create in case it doesn't exist
     */
    fun createDirectoryIfNotExists(file: File) {
        if(!file.exists()) {
            Log.i("FilesUtil", "Creating folder: ${file.absolutePath}")
            file.mkdir()
        }
    }
}