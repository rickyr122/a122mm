package com.example.a122mm.update

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

class UpdateRepository(
    private val updateApi: UpdateApiService
) {
    suspend fun fetchRemote(): Result<VersionInfo> = try {
        Result.success(updateApi.version())
    } catch (t: Throwable) {
        Result.failure(t)
    }

    fun needsUpdate(remote: VersionInfo, currentCode: Long): Boolean =
        remote.versionCode > currentCode

    fun isForced(remote: VersionInfo, currentCode: Long): Boolean =
        remote.minVersionCode?.let { currentCode < it } == true

    /** Returns DownloadManager enqueue id */
    fun downloadApk(context: Context, url: String): Long {
        val req = DownloadManager.Request(Uri.parse(url))
            .setTitle("122 Movies Update")
            .setDescription("Downloading update…")
            .setMimeType("application/vnd.android.package-archive")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "122movies-latest.apk"
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return dm.enqueue(req)
    }
}
