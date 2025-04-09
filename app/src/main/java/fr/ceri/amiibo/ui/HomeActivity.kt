package fr.ceri.amiibo.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import fr.ceri.amiibo.data.api.ApiClient
import fr.ceri.amiibo.data.realm.GameSeriesRealm
import fr.ceri.amiibo.utils.UserSettingsManager
import fr.ceri.amiibo.databinding.ActivityHomeBinding
import io.realm.Realm
import kotlinx.coroutines.*

/**
 * Activité d'accueil de l'application.
 * Affiche le meilleur score, permet de choisir le mode de jeu, de consulter les règles,
 * et d'accéder au jeu une fois les données chargées depuis l’API.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var ui: ActivityHomeBinding
    private var isLoading = true

    override fun onCreate(savedInstanceState: Bundle?) {

        // Affichage de l’écran de démarrage natif (SplashScreen)
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { isLoading } // Garde l’écran tant que les données ne sont pas prêtes

        super.onCreate(savedInstanceState)
        ui = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(ui.root)

        // Affiche le meilleur score depuis les préférences utilisateur
        CoroutineScope(Dispatchers.Main).launch {
            setupBestScore()
        }

        setupButtons()

        // Chargement asynchrone des données de séries de jeux depuis l’API
        fetchGameSeriesData()
    }

    /**
     * Récupère les données de séries de jeux depuis l’API et les enregistre dans la base de données locale.
     */
    private fun fetchGameSeriesData() {
        ui.btnPlay.isEnabled = false // Désactive le bouton "Jouer" tant que les données ne sont pas prêtes

        CoroutineScope(Dispatchers.IO).launch {
            try {

                //* Appel à l'API pour récupérer les séries de jeux
                val response = ApiClient.apiService.getGameSeries()
                val result = response.body()?.amiibo?.distinctBy { it.name } ?: emptyList()

                //* Vérification de la réponse de l'API, si :
                //? - la réponse est réussie : alors on traite les données et on les enregistre dans Realm
                //! - la réponse échoue : on affiche un message d'erreur
                //! et on désactive le bouton "Jouer"
                if (response.isSuccessful && result.isNotEmpty()) {

                    // Mise à jour de la base Realm locale avec les nouvelles séries de jeux
                    val realm = Realm.getDefaultInstance()
                    realm.executeTransaction { transactionRealm ->
                        transactionRealm.delete(GameSeriesRealm::class.java) // Nettoie les anciennes entrées
                        result.forEach {
                            transactionRealm.createObject(GameSeriesRealm::class.java, it.name)
                        }
                    }

                    // Active le bouton "Jouer" une fois le chargement terminé
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

    /**
     * Configure les écouteurs de clics sur les boutons de la page d’accueil.
     */
    private fun setupButtons() {
        ui.btnPlay.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java)) // Lancer le jeu
        }

        ui.btnMode.setOnClickListener {
            modeChoices() // Ouvre le sélecteur de difficulté
        }

        ui.btnRules.setOnClickListener {
            startActivity(Intent(this, RuleActivity::class.java)) // Affiche les règles du jeu
        }

        ui.footerIconLeft.setOnClickListener {
            finish() // Ferme l’activité et retourne sur l'écran d'accueil du téléphone
        }
    }

    /**
     * Récupère et affiche le meilleur score enregistré.
     */
    private suspend fun setupBestScore() {
        val bestScore = UserSettingsManager.getBestScore()
        ui.tvScore.text = bestScore.toString()
    }

    /**
     * Affiche une boîte de dialogue pour choisir le mode de jeu (nombre de questions).
     */
    private fun modeChoices() {
        CoroutineScope(Dispatchers.Main).launch {

            // Options de difficulté
            val modes = arrayOf("Facile (5 questions)", "Moyen (10 questions)", "Difficile (15 questions)")
            val values = arrayOf(5, 10, 15)
            val current = UserSettingsManager.getQuestionCount()
            val selectedIndex = values.indexOf(current)

            // Affiche la boîte de dialogue avec les options
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

    /**
     * Affiche un message d’erreur dans un Toast et désactive le bouton "Jouer".
     */
    private suspend fun showLoadingError(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@HomeActivity, message, Toast.LENGTH_LONG).show()
            ui.btnPlay.isEnabled = false
        }
    }
}
