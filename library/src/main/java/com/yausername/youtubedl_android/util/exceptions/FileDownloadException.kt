package com.yausername.youtubedl_android.util.exceptions

class FileDownloadException: Exception {
    constructor(message: String?) : super(message) {}
    constructor(message: String?, e: Throwable?) : super(message, e) {}
    constructor(e: Throwable?) : super(e) {}
}
