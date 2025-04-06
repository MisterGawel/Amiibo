package fr.ceri.amiibo.utils

import android.content.Context
import android.media.MediaPlayer
import fr.ceri.amiibo.R

object MusicManager {
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

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

    fun pauseMusic() {
        mediaPlayer?.pause()
        isPlaying = false
    }

    fun resumeMusic() {
        if (!isPlaying) {
            mediaPlayer?.start()
            isPlaying = true
        }
    }

    fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
    }
}