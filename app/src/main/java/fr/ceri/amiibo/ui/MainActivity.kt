package fr.ceri.amiibo.ui

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import fr.ceri.amiibo.R
import fr.ceri.amiibo.data.api.ApiClient
import fr.ceri.amiibo.data.model.Amiibo
import fr.ceri.amiibo.data.model.AmiiboGame
import fr.ceri.amiibo.data.realm.AmiiboRealm
import fr.ceri.amiibo.data.realm.GameSeriesRealm
import fr.ceri.amiibo.databinding.ActivityMainBinding
import io.realm.Realm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var ui: ActivityMainBinding
    private lateinit var realm: Realm
    private lateinit var adapter: ArrayAdapter<String>
    private val selectedGameseries = mutableListOf<AmiiboGame>()
    private val allGameseries = mutableListOf<AmiiboGame>()
    private var allSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {

        //* Initialisation de l'UI
        super.onCreate(savedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)
        setSupportActionBar(ui.toolbar)
        ui.title.text = Html.fromHtml(
            "<font color='#000000'>Choisis tes </font><font color='#C8007D'>catégories</font>",
            Html.FROM_HTML_MODE_LEGACY
        )

        //* Initialisation de Realm
        realm = Realm.getDefaultInstance()
        val storedSeries = realm.where(GameSeriesRealm::class.java).findAll()
        if (storedSeries.isEmpty()) {
            Toast.makeText(this, "Aucune donnée disponible", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        allGameseries.addAll(storedSeries.map {
            AmiiboGame(key = it.name, name = it.name)
        })

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_multiple_choice,
            allGameseries.map { it.name }
        )

        ui.listViewCategories.adapter = adapter
        ui.listViewCategories.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        //* Sélection par défaut aléatoire de 4 catégories de gameseries
        val randomIndices = allGameseries.indices.shuffled().take(4)
        randomIndices.forEach { index ->
            ui.listViewCategories.setItemChecked(index, true)
        }

        ui.btnStartGame.setOnClickListener {
            handleStartGame()
        }

        ui.footerIconLeft.setOnClickListener {
            finish()
        }
    }

    private fun handleStartGame() {
        val checked = ui.listViewCategories.checkedItemPositions
        selectedGameseries.clear()

        for (i in 0 until adapter.count) {
            if (checked.get(i)) {
                selectedGameseries.add(allGameseries[i])
            }
        }

        if (selectedGameseries.size < 4) {
            Toast.makeText(this, "Sélectionne au moins 4 catégories !", Toast.LENGTH_SHORT).show()
            return
        }

        ui.loaderLottie.visibility = View.VISIBLE
        ui.container.alpha = 0.5f
        ui.btnStartGame.isEnabled = false
        ui.listViewCategories.isEnabled = false
        ui.footerIconLeft.isEnabled = false

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

            Realm.getDefaultInstance().use { localRealm ->
                localRealm.executeTransaction { transactionRealm ->
                    transactionRealm.delete(AmiiboRealm::class.java)

                    allAmiibos.forEach { apiAmiibo ->
                        val id = "${apiAmiibo.name}_${apiAmiibo.gameSeries}"

                        // Juste au cas où
                        transactionRealm.where(AmiiboRealm::class.java)
                            .equalTo("id", id)
                            .findFirst()
                            ?.deleteFromRealm()

                        val amiibo = transactionRealm.createObject(AmiiboRealm::class.java, id)
                        amiibo.name = apiAmiibo.name
                        amiibo.gameSeries = apiAmiibo.gameSeries
                        amiibo.image = apiAmiibo.image
                    }
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Chargement terminé, bon jeu !", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@MainActivity, GameActivity::class.java))
            }
        }
    }

    //* Menu d'options
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_getamiibos, menu)
        return true
    }

    //* Gestion des clics sur le menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_select_all -> {
                toggleSelectAll()
                // Change l’icône dynamiquement
                item.setIcon(
                    if (allSelected) R.drawable.checkbox
                    else R.drawable.uncheckbox
                )
                true
            }
            R.id.action_validate -> {
                handleStartGame()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleSelectAll() {
        allSelected = !allSelected
        for (i in 0 until adapter.count) {
            ui.listViewCategories.setItemChecked(i, allSelected)
        }
        adapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}