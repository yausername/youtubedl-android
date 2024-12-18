package com.yausername.youtubedl_android_example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
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
import java.util.regex.Pattern
import kotlin.jvm.functions.Function3


class CommandExampleActivity : AppCompatActivity(), View.OnClickListener {
    private var btnRunCommand: Button? = null
    private var btnStopDownload: Button? = null
    private var etCommand: EditText? = null
    private var progressBar: ProgressBar? = null
    private var tvCommandStatus: TextView? = null
    private var tvCommandOutput: TextView? = null
    private var pbLoading: ProgressBar? = null

    private var running = false
    private val compositeDisposable = CompositeDisposable()
    private val processId = "MyMainDownload"

    private val callback: (Float, Long?, String?) -> Unit =
        { progress: Float, o2: Long?, line: String? ->
            runOnUiThread {
                progressBar!!.progress = progress.toInt()
                tvCommandStatus!!.text = line
            }
            Unit
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_command_example)

        initViews()
        initListeners()
    }

    private fun initViews() {
        btnRunCommand = findViewById(R.id.btn_run_command)
        btnStopDownload = findViewById(R.id.btn_stop_download)
        etCommand = findViewById(R.id.et_command)
        progressBar = findViewById(R.id.progress_bar)
        tvCommandStatus = findViewById(R.id.tv_status)
        pbLoading = findViewById(R.id.pb_status)
        tvCommandOutput = findViewById(R.id.tv_command_output)
    }

    private fun initListeners() {
        btnRunCommand!!.setOnClickListener(this)
        btnStopDownload!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_run_command -> runCommand()
            R.id.btn_stop_download -> if (running) {
                try {
                    getInstance().destroyProcessById(processId)
                    running = false
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                }
            }
        }
    }

    private fun runCommand() {
        if (running) {
            Toast.makeText(
                this@CommandExampleActivity,
                "cannot start command. a command is already in progress",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (!isStoragePermissionGranted) {
            Toast.makeText(
                this@CommandExampleActivity,
                "grant storage permission and retry",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val command = etCommand!!.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(command)) {
            etCommand!!.error = getString(R.string.command_error_1)
            return
        }

        // this is not the recommended way to add options/flags/url and might break in future
        // use the constructor for url, addOption(key) for flags, addOption(key, value) for options
        val request = YoutubeDLRequest(emptyList())
        val commandRegex = "\"([^\"]*)\"|(\\S+)"
        val m = Pattern.compile(commandRegex).matcher(command)
        while (m.find()) {
            if (m.group(1) != null) {
                request.addOption(m.group(1))
            } else {
                request.addOption(m.group(2))
            }
        }

        showStart()

        running = true
        val disposable =
            Observable.fromCallable {
                getInstance().execute(
                    request, processId,
                    callback
                )
            }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ youtubeDLResponse: YoutubeDLResponse ->
                    pbLoading!!.visibility = View.GONE
                    progressBar!!.progress = 100
                    tvCommandStatus!!.text = getString(R.string.command_complete)
                    tvCommandOutput!!.text = youtubeDLResponse.out
                    Toast.makeText(
                        this@CommandExampleActivity,
                        "command successful",
                        Toast.LENGTH_LONG
                    ).show()
                    running = false
                }, { e: Throwable ->
                    if (BuildConfig.DEBUG) Log.e(TAG, "command failed", e)
                    pbLoading!!.visibility = View.GONE
                    tvCommandStatus!!.text = getString(R.string.command_failed)
                    tvCommandOutput!!.text = e.message
                    Toast.makeText(this@CommandExampleActivity, "command failed", Toast.LENGTH_LONG)
                        .show()
                    running = false
                })
        compositeDisposable.add(disposable)
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun showStart() {
        tvCommandStatus!!.text = getString(R.string.command_start)
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
        private val TAG: String = CommandExampleActivity::class.java.simpleName
    }
}