package fr.ceri.amiibo.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Récepteur de diffusion (BroadcastReceiver) utilisé pour gérer la lecture de la musique
 * en fonction des événements système (ex : écran éteint, utilisateur actif, etc.).
 */
class MusicReceiver : BroadcastReceiver() {

    // Méthode appelée automatiquement lors de la réception d'un broadcast
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {

            // Lorsque l'écran s’éteint, on met la musique en pause
            Intent.ACTION_SCREEN_OFF -> {
                MusicManager.pauseMusic()
            }

            // Lorsque l’utilisateur déverrouille l’écran, on relance la musique
            Intent.ACTION_USER_PRESENT -> {
                MusicManager.startMusic(context!!)
            }

            // Lorsque des boîtes de dialogue système se ferment (ex : bouton power), on met en pause la musique
            Intent.ACTION_CLOSE_SYSTEM_DIALOGS -> {
                MusicManager.pauseMusic()
            }
        }
    }
}
