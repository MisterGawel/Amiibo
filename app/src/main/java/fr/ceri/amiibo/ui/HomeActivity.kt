package fr.ceri.amiibo.ui

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import fr.ceri.amiibo.R
import fr.ceri.amiibo.data.api.ApiClient
import fr.ceri.amiibo.data.realm.GameSeriesRealm
import fr.ceri.amiibo.data.realm.UserSettingsManager
import fr.ceri.amiibo.databinding.ActivityHomeBinding
import io.realm.Realm
import kotlinx.coroutines.*

class HomeActivity : AppCompatActivity() {

    private lateinit var ui: ActivityHomeBinding
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        //* Ecran de démarrage de l’application
        val splashScreen = installSplashScreen()
        var isLoading = true
        splashScreen.setKeepOnScreenCondition { isLoading }

        //* Initialisation de l'écran
        super.onCreate(savedInstanceState)
        ui = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(ui.root)

        //* Récupération de la musique et du meilleur score
        CoroutineScope(Dispatchers.Main).launch {
            setupMusicToggle()
            setupBestScore()
        }

        setupButtons()

        //* Coroutine pour récupérer les séries de jeux Amiibo via l'API
        ui.btnPlay.isEnabled = false
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.getGameSeries()
                val result = response.body()?.amiibo?.distinctBy { it.name } ?: emptyList()

                if (response.isSuccessful && result.isNotEmpty()) {

                    //* Enregistrement des séries de jeux dans Realm
                    val realm = Realm.getDefaultInstance()
                    realm.executeTransaction { transactionRealm ->
                        transactionRealm.delete(GameSeriesRealm::class.java)

                        result.forEach {
                            val gameSeries = transactionRealm.createObject(GameSeriesRealm::class.java, it.name)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        isLoading = false
                        ui.btnPlay.isEnabled = true
                    }
                } else {
                    showLoadingError("Erreur lors du chargement des catégories.")
                    isLoading = false
                }
            } catch (e: Exception) {
                Log.e("HomeActivity", "Erreur API", e)
                showLoadingError("Erreur réseau : ${e.message}")
                isLoading = false
            }
        }
    }

    //* Configuration des boutons
    private fun setupButtons() {
        ui.btnPlay.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        ui.btnMode.setOnClickListener {
            modeChoices()
        }

        ui.btnRules.setOnClickListener {
            startActivity(Intent(this, RuleActivity::class.java))
        }

        ui.footerIconLeft.setOnClickListener {
            finish()
        }

        ui.footerIconRight.setOnClickListener {
            toggleMusic()
        }
    }

    //* Configuration de la musique
    private suspend fun setupMusicToggle() {
        val isMusicEnabled = UserSettingsManager.isMusicEnabled()
        ui.footerIconRight.setImageResource(if (isMusicEnabled) R.drawable.musicon else R.drawable.musicoff)

        if (isMusicEnabled) {
            mediaPlayer = MediaPlayer.create(this, R.raw.background_music)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        }
    }

    //* Configuration du meilleur score
    private suspend fun setupBestScore() {
        val bestScore = UserSettingsManager.getBestScore()
        ui.tvScore.text = bestScore.toString()
    }

    //* Choix du mode de jeu
    private fun modeChoices(){
        CoroutineScope(Dispatchers.Main).launch {
            val modes = arrayOf("Facile (10 questions)", "Moyen (20 questions)", "Difficile (30 questions)")
            val values = arrayOf(10, 20, 30)
            val current = UserSettingsManager.getQuestionCount()
            val selectedIndex = values.indexOf(current)

            AlertDialog.Builder(this@HomeActivity)
                .setTitle("Choisis un mode de jeu")
                .setSingleChoiceItems(modes, selectedIndex) { dialog, which ->
                    CoroutineScope(Dispatchers.IO).launch {
                        UserSettingsManager.setQuestionCount(values[which])
                    }
                    Toast.makeText(this@HomeActivity, "Mode sélectionné : ${modes[which]}", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Annuler", null)
                .show()
        }
    }

    //* Gestion de la musique
    private fun toggleMusic(){
        CoroutineScope(Dispatchers.Main).launch {
            val current = UserSettingsManager.isMusicEnabled()
            UserSettingsManager.setMusicEnabled(!current)

            val newState = !current
            ui.footerIconRight.setImageResource(if (newState) R.drawable.musicon else R.drawable.musicoff)

            if (newState) {
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(this@HomeActivity, R.raw.background_music)
                    mediaPlayer?.isLooping = true
                }
                mediaPlayer?.start()
            } else {
                mediaPlayer?.pause()
            }

            Toast.makeText(
                this@HomeActivity,
                if (newState) "Musique activée" else "Musique coupée",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private suspend fun showLoadingError(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@HomeActivity, message, Toast.LENGTH_LONG).show()
            ui.btnPlay.isEnabled = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}