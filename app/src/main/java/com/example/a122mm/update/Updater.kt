package com.example.a122mm.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

data class UpdateState(
    val isDownloading: Boolean = false,
    val progress: Int = 0,                 // 0..100
    val error: String? = null
)

class Updater(private val ctx: Context) {

    private val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    /**
     * Start downloading the APK and emit progress. When finished, automatically launch the installer UI.
     */
    fun downloadAndInstall(url: String, fileName: String = "app-update.apk"): Flow<UpdateState> = flow {
        // Kick off download to app-scoped external dir (no storage permission needed)
        val req = DownloadManager.Request(Uri.parse(url))
            .setTitle("Downloading update")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setMimeType("application/vnd.android.package-archive")
            .setDestinationInExternalFilesDir(ctx, Environment.DIRECTORY_DOWNLOADS, fileName)

        val id = dm.enqueue(req)
        emit(UpdateState(isDownloading = true, progress = 0))

        // Poll progress
        var done = false
        var last = -1
        while (!done) {
            delay(400)
            val q = DownloadManager.Query().setFilterById(id)
            dm.query(q).use { c ->
                if (c != null && c.moveToFirst()) {
                    val bytes = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val total = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    val status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    val pct = if (total > 0) ((bytes * 100) / total).toInt() else 0
                    if (pct != last) {
                        emit(UpdateState(isDownloading = true, progress = pct))
                        last = pct
                    }
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            done = true
                            emit(UpdateState(isDownloading = false, progress = 100))
                            val uri = dm.getUriForDownloadedFile(id)
                                ?: throw IllegalStateException("Downloaded file URI missing")
                            launchInstaller(uri)
                        }
                        DownloadManager.STATUS_FAILED -> {
                            val reason = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                            emit(UpdateState(isDownloading = false, progress = 0, error = "Download failed ($reason)"))
                            return@flow
                        }
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun launchInstaller(uri: Uri) {
        // Android 8+: user must allow installs from this app once
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !ctx.packageManager.canRequestPackageInstalls()
        ) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                .setData(Uri.parse("package:${ctx.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(intent)
            Toast.makeText(ctx, "Allow installs, then press Update again.", Toast.LENGTH_LONG).show()
            return
        }

        val install = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(install) // hands off to system installer UI
    }
}
