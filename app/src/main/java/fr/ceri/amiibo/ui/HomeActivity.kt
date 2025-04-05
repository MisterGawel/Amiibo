package fr.ceri.amiibo.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import fr.ceri.amiibo.databinding.ActivityHomeBinding
import fr.ceri.amiibo.data.api.ApiClient
import fr.ceri.amiibo.shared.SharedGameSeries
import fr.ceri.amiibo.shared.SharedSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity : AppCompatActivity() {

    private lateinit var ui: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash Android 12+
        val splashScreen = installSplashScreen()
        SharedSettings.init(applicationContext)

        var isLoading = true
        splashScreen.setKeepOnScreenCondition { isLoading }

        super.onCreate(savedInstanceState)
        ui = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(ui.root)

        ui.btnPlay.isEnabled = false

        // Charger les categories depuis l'API
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.getGameSeries()
                if (response.isSuccessful && response.body() != null) {
                    SharedGameSeries.data = response.body()?.amiibo
                        ?.distinctBy { it.name }
                        ?: emptyList()

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

        setupButtons()
    }

    private fun setupButtons() {
        ui.btnPlay.setOnClickListener {
            if (SharedGameSeries.data.isEmpty()) {
                Toast.makeText(this, "Les données ne sont pas encore prêtes.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, MainActivity::class.java))
        }

        ui.btnLanguages.setOnClickListener {
            startActivity(Intent(this, LanguageActivity::class.java))
        }

        ui.btnRules.setOnClickListener {
            startActivity(Intent(this, RuleActivity::class.java))
        }
    }

    private suspend fun showLoadingError(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@HomeActivity, message, Toast.LENGTH_LONG).show()
            ui.btnPlay.isEnabled = false
        }
    }
}
