package fr.ceri.amiibo.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.ceri.amiibo.utils.UserSettingsManager
import fr.ceri.amiibo.databinding.ActivityGameoverBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activité affichée lorsque le jeu est terminé.
 * Elle permet d'afficher le score final de l'utilisateur, et d'enregistrer ce score
 * s'il dépasse le meilleur score précédent.
 */
class GameOverActivity : AppCompatActivity() {

    private lateinit var ui: ActivityGameoverBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        //* Initialisation de l'UI
        super.onCreate(savedInstanceState)
        ui = ActivityGameoverBinding.inflate(layoutInflater)
        setContentView(ui.root)
        setupListeners()

        // Récupération du score transmis depuis GameActivity
        val score = intent.getIntExtra("score", 0)

        // Lancement d'une coroutine pour comparaison/stockage du meilleur score
        CoroutineScope(Dispatchers.IO).launch {
            val bestScore = UserSettingsManager.getBestScore()
            if (score > bestScore) {
                UserSettingsManager.setBestScore(score) // Mise à jour du score si supérieur
            }

            // Mise à jour de l'UI avec le score final
            withContext(Dispatchers.Main) {
                ui.tvFinalScore.text = "$score"
            }
        }
    }

    /**
     * Initialise les boutons de l'écran de fin de partie.
     * - "Rejouer" ferme simplement l'activité (retour à l'écran précédent).
     * - "Accueil" redirige vers HomeActivity en nettoyant la pile d'activités.
     */
    private fun setupListeners() {
        // Bouton pour rejouer (revient à GameActivity via back stack)
        ui.btnReplay.setOnClickListener {
            finish()
        }

        // Bouton pour retourner à l'écran d'accueil
        ui.btnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)

            // Empêche le retour à l'écran précédent (nettoie la pile d'activités)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }
}
