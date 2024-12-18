package com.yausername.youtubedl_android_example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.yausername.youtubedl_android.YoutubeDL.getInstance
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.YoutubeDLResponse
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import kotlin.jvm.functions.Function3

class DownloadingExampleActivity : AppCompatActivity(), View.OnClickListener {
    private var btnStartDownload: Button? = null
    private var btnStopDownload: Button? = null
    private var etUrl: EditText? = null
    private var useConfigFile: Switch? = null
    private var progressBar: ProgressBar? = null
    private var tvDownloadStatus: TextView? = null
    private var tvCommandOutput: TextView? = null
    private var pbLoading: ProgressBar? = null

    private var downloading = false
    private val compositeDisposable = CompositeDisposable()
    private val processIdFacebook = "Myfacebookprocess"
    private val processIdCbc = "MyCbcprocess"


    private val callback =
        { progress: Float, o2: Long?, line: String? ->
            runOnUiThread {
                progressBar!!.progress = progress.toInt()
                tvDownloadStatus!!.text = line
            }
            Unit
        }
    var progressCallback: Function2<Int?, String?, Unit> = { size: Int?, line: String? ->
        // Your implementation of the progressCallback function
        Log.e(TAG, "FFMPEG size: $line")
        null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloading_example)

        initViews()
        initListeners()
    }

    private fun initViews() {
        btnStartDownload = findViewById(R.id.btn_start_download)
        btnStopDownload = findViewById(R.id.btn_stop_download)
        etUrl = findViewById(R.id.et_url)
        (etUrl as EditText).setText("https://www.cbsnews.com/video/a-nation-in-transition-cbs-reports")
        useConfigFile = findViewById(R.id.use_config_file)
        progressBar = findViewById(R.id.progress_bar)
        tvDownloadStatus = findViewById(R.id.tv_status)
        pbLoading = findViewById(R.id.pb_status)
        tvCommandOutput = findViewById(R.id.tv_command_output)
    }

    private fun initListeners() {
        btnStartDownload!!.setOnClickListener(this)
        btnStopDownload!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_start_download -> {
                val cbcurl =
                    "https://baijiahao.baidu.com/s?id=1783258486309390969" //"https://www.cbsnews.com/video/a-nation-in-transition-cbs-reports";
                startDownload(cbcurl, processIdCbc)
            }

            R.id.btn_stop_download -> try {
                getInstance().destroyProcessById(processIdCbc)
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
    }

    private fun startDownload(url: String, processId: String) {
        /*if (downloading) {
            Toast.makeText(DownloadingExampleActivity.this, "cannot start download. a download is already in progress", Toast.LENGTH_LONG).show();
            return;F
        }*/

        if (!isStoragePermissionGranted) {
            Toast.makeText(
                this@DownloadingExampleActivity,
                "grant storage permission and retry",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (TextUtils.isEmpty(url)) {
            etUrl!!.error = getString(R.string.url_error)
            return
        }
        val request = YoutubeDLRequest(url)
        val youtubeDLDir = downloadLocation
        val config = File(youtubeDLDir, "config.txt")

        if (useConfigFile!!.isChecked && config.exists()) {
            request.addOption("--config-location", config.absolutePath)
        } else {
            request.addOption("--no-mtime")
            request.addOption("--downloader", "ffmpeg")
            //request.addOption("-f", "bestvideo+bestaudio");
            request.addOption("-o", youtubeDLDir.absolutePath + "/%(title)s.%(ext)s")
        }

        showStart()


        downloading = true
        val disposable = Observable.fromCallable {
            getInstance().execute(
                request,
                processId,
                callback,
                progressCallback
            )
        }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ youtubeDLResponse: YoutubeDLResponse ->
                pbLoading!!.visibility = View.GONE
                progressBar!!.progress = 100
                tvDownloadStatus!!.text = getString(R.string.download_complete)
                tvCommandOutput!!.text = youtubeDLResponse.out
                Toast.makeText(
                    this@DownloadingExampleActivity,
                    "download successful",
                    Toast.LENGTH_LONG
                ).show()
                downloading = false
            }, { e: Throwable ->
                if (BuildConfig.DEBUG) Log.e(TAG, "failed to download " + e.message)
                pbLoading!!.visibility = View.GONE
                tvDownloadStatus!!.text = getString(R.string.download_failed)
                tvCommandOutput!!.text = e.message
                Toast.makeText(
                    this@DownloadingExampleActivity,
                    "download failed",
                    Toast.LENGTH_LONG
                ).show()
                downloading = false
            })
        compositeDisposable.add(disposable)
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private val downloadLocation: File
        get() {
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val youtubeDLDir = File(downloadsDir, "youtubedl-android")
            if (!youtubeDLDir.exists()) youtubeDLDir.mkdir()
            return youtubeDLDir
        }

    private fun showStart() {
        tvDownloadStatus!!.text = getString(R.string.download_start)
        progressBar!!.progress = 0
        pbLoading!!.visibility = View.VISIBLE
    }

    val isStoragePermissionGranted: Boolean
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    return true
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        1
                    )
                    return false
                }
            } else {
                return true
            }
        }

    companion object {
        // Define the onComplete function
        private val TAG: String = DownloadingExampleActivity::class.java.simpleName
    }
}