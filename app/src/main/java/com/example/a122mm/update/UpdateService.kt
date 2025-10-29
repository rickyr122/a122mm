package com.example.a122mm.update

import android.app.DownloadManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

class UpdateService : Service() {

    companion object {
        const val ACTION_START = "start_update"
        const val EXTRA_URL = "url"
        const val EXTRA_NAME = "fileName"
        private const val CH_ID = "update_channel"
        private const val NOTIF_ID = 4901

        fun start(context: Context, url: String, fileName: String = "app-update.apk") {
            val i = Intent(context, UpdateService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_NAME, fileName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(i)
            } else {
                context.startService(i)
            }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var dm: DownloadManager
    private var downloadId: Long = -1L

    override fun onCreate() {
        super.onCreate()
        dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_START) {
            val url = intent.getStringExtra(EXTRA_URL) ?: return START_NOT_STICKY
            val name = intent.getStringExtra(EXTRA_NAME) ?: "app-update.apk"
            scope.launch { runDownload(url, name) }
            return START_STICKY
        }
        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?) = null

    private fun runDownload(url: String, fileName: String) {
        val req = DownloadManager.Request(Uri.parse(url))
            .setTitle("Downloading update")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setMimeType("application/vnd.android.package-archive")
            // app-scoped external dir (no permission needed)
            .setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, fileName)

        downloadId = dm.enqueue(req)
        UpdateBus.setProgress(0)
        startForeground(NOTIF_ID, buildNotif(0))

        scope.launch {
            var last = -1
            var finished = false
            while (!finished) {
                delay(400)
                val q = DownloadManager.Query().setFilterById(downloadId)
                dm.query(q).use { c ->
                    if (c != null && c.moveToFirst()) {
                        val status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        val bytes = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val total = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        val pct = if (total > 0) ((bytes * 100) / total).toInt() else 0
                        if (pct != last) {
                            last = pct
                            UpdateBus.setProgress(pct)
                            notifyProgress(pct)
                        }
                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                finished = true
                                UpdateBus.setProgress(100)
                                notifyProgress(100)
                                val uri = dm.getUriForDownloadedFile(downloadId)
                                    ?: throw IllegalStateException("Downloaded file URI missing")
                                launchInstaller(uri)
                            }
                            DownloadManager.STATUS_FAILED -> {
                                finished = true
                                UpdateBus.reset()
                                stopForeground(STOP_FOREGROUND_REMOVE)
                                stopSelf()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun buildNotif(pct: Int): Notification {
        val builder = NotificationCompat.Builder(this, CH_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Updating app")
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setProgress(100, max(pct, 0), pct < 0)
        return builder.build()
    }

    private fun notifyProgress(pct: Int) {
        val n = buildNotif(pct)
        NotificationManagerCompat.from(this).notify(NOTIF_ID, n)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel(CH_ID, "App Updates", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    private fun launchInstaller(uri: Uri) {
        // Stop foreground before launching installer UI
        stopForeground(STOP_FOREGROUND_DETACH)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !packageManager.canRequestPackageInstalls()
        ) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                .setData(Uri.parse("package:$packageName"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            stopSelf()
            return
        }

        val install = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(install)

        // service can finish; installer takes over
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
