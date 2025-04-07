package fr.ceri.amiibo.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.ceri.amiibo.data.realm.UserSettingsManager
import fr.ceri.amiibo.databinding.ActivityGameoverBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameOverActivity : AppCompatActivity() {

    private lateinit var ui: ActivityGameoverBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        //* Initialisation de l'UI
        super.onCreate(savedInstanceState)
        ui = ActivityGameoverBinding.inflate(layoutInflater)
        setContentView(ui.root)
        setupListeners()

        //* Récupération du score & création d'une coroutine pour le stocker
        //* dans la base de données si c'est le meilleur score
        val score = intent.getIntExtra("score", 0)
        CoroutineScope(Dispatchers.IO).launch {
            val bestScore = UserSettingsManager.getBestScore()
            if (score > bestScore) {
                UserSettingsManager.setBestScore(score)
            }

            //* Affichage du score dans l’UI
            withContext(Dispatchers.Main) {
                ui.tvFinalScore.text = "$score"
            }
        }
    }

    private fun setupListeners() {
        ui.btnReplay.setOnClickListener {
            finish()
        }

        ui.btnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)

            //* Ajout de flags pour éviter de revenir à l'écran de jeu (nettoyage de la pile d'activités)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }
}