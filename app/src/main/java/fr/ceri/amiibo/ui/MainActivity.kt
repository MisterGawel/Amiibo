package fr.ceri.amiibo.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import fr.ceri.amiibo.data.api.ApiClient
import fr.ceri.amiibo.data.model.Amiibo
import fr.ceri.amiibo.data.model.AmiiboGame
import fr.ceri.amiibo.databinding.ActivityMainBinding
import fr.ceri.amiibo.shared.SharedAmiiboList
import fr.ceri.amiibo.shared.SharedGameSeries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.view.View

class MainActivity : AppCompatActivity() {

    private lateinit var ui: ActivityMainBinding
    private lateinit var adapter: ArrayAdapter<String>
    private val selectedGameseries = mutableListOf<AmiiboGame>()
    private val allGameseries = mutableListOf<AmiiboGame>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        // Charger les données depuis SharedGameSeries
        val data = SharedGameSeries.data
        if (data.isEmpty()) {
            Toast.makeText(this, "Aucune donnée disponible", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        allGameseries.addAll(data)

        // Adapter des noms
        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_multiple_choice,
            data.map { it.name }
        )

        ui.listViewCategories.adapter = adapter
        ui.listViewCategories.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        // Gérer clic sur bouton "Démarrer"
        ui.btnStartGame.setOnClickListener {
            val checked = ui.listViewCategories.checkedItemPositions
            selectedGameseries.clear()
            ui.loaderLottie.visibility = View.VISIBLE

            for (i in 0 until adapter.count) {
                if (checked.get(i)) {
                    selectedGameseries.add(allGameseries[i])
                }
            }

            if (selectedGameseries.size < 4) {
                Toast.makeText(this, "Sélectionne au moins 4 catégories !", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 📥 Télécharger les amiibos pour les séries sélectionnées
            CoroutineScope(Dispatchers.IO).launch {
                val allAmiibos = mutableListOf<Amiibo>()

                for (game in selectedGameseries) {
                    try {
                        val response = ApiClient.apiService.getAmiiboByGameSeries(game.name)
                        if (response.isSuccessful) {
                            val result = response.body()?.amiibo ?: emptyList()
                            Log.d("MainActivity", "→ ${game.name} : ${result.size} amiibos")
                            allAmiibos.addAll(result)
                        } else {
                            Log.w("MainActivity", "API échouée pour ${game.name}")
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Erreur API pour ${game.name}", e)
                    }
                }

                withContext(Dispatchers.Main) {
                    if (allAmiibos.isEmpty()) {
                        Toast.makeText(
                            this@MainActivity,
                            "Aucun amiibo trouvé pour ta sélection.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@withContext
                    }

                    SharedAmiiboList.list = allAmiibos
                    Toast.makeText(this@MainActivity, "Chargement terminé, bon jeu !", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@MainActivity, GameActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}
