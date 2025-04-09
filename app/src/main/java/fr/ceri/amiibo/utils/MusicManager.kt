package fr.ceri.amiibo.utils

import android.content.Context
import android.media.MediaPlayer
import fr.ceri.amiibo.R

/**
 * Objet chargé de gérer la lecture de la musique de fond dans l'application.
 * Fournit des fonctions pour démarrer, mettre en pause, reprendre et arrêter la musique.
 */
object MusicManager {
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    /**
     * Démarre la musique si elle n'est pas déjà lancée.
     * Initialise le MediaPlayer si nécessaire, en mode boucle.
     */
    fun startMusic(context: Context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.background_music)
            mediaPlayer?.isLooping = true
        }

        if (!isPlaying) {
            mediaPlayer?.start()
            isPlaying = true
        }
    }

    /**
     * Met la musique en pause, si elle est en cours de lecture.
     */
    fun pauseMusic() {
        mediaPlayer?.pause()
        isPlaying = false
    }

    /**
     * Arrête complètement la musique et libère les ressources associées.
     * À utiliser lorsque la musique ne doit plus être utilisée du tout.
     */
    fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
    }
}
