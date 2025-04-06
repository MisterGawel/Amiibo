package fr.ceri.amiibo.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MusicReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SCREEN_OFF -> {
                MusicManager.pauseMusic()
            }

            Intent.ACTION_USER_PRESENT -> {
                MusicManager.startMusic(context!!)
            }

            Intent.ACTION_CLOSE_SYSTEM_DIALOGS -> {
                MusicManager.pauseMusic()
            }
        }
    }
}