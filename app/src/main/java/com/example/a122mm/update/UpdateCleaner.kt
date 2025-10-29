package com.example.a122mm.update

import android.content.Context
import android.os.Environment
import android.util.Log

object UpdateCleaner {
    /**
     * Deletes any leftover update APKs from the app's private download directory.
     * Call this once on app startup (in Application.onCreate or MainActivity.onCreate).
     */
    fun cleanOldApks(context: Context) {
        try {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            if (dir != null && dir.exists()) {
                val deleted = mutableListOf<String>()
                dir.listFiles()?.forEach { f ->
                    if (f.name.endsWith(".apk", ignoreCase = true)) {
                        val ok = f.delete()
                        if (ok) deleted.add(f.name)
                    }
                }
                if (deleted.isNotEmpty()) {
                    Log.i("UpdateCleaner", "Deleted old APKs: $deleted")
                }
            }
        } catch (e: Exception) {
            Log.e("UpdateCleaner", "Failed to clean old APKs", e)
        }
    }
}
