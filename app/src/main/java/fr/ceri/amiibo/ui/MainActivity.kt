package fr.ceri.amiibo.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
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
import fr.ceri.amiibo.utils.UserSettingsManager
import fr.ceri.amiibo.databinding.ActivityMainBinding
import io.realm.Realm
import kotlinx.coroutines.*

/**
 * Activité principale permettant à l'utilisateur de sélectionner des catégories
 * (séries de jeux Amiibo) avant de commencer une partie.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var ui: ActivityMainBinding
    private lateinit var realm: Realm
    private lateinit var adapter: ArrayAdapter<String>

    // Catégories sélectionnées et disponibles
    private val selectedGameseries = mutableListOf<AmiiboGame>()
    private val allGameseries = mutableListOf<AmiiboGame>()
    private var allSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)
        setSupportActionBar(ui.toolbar)
        supportActionBar?.title = ""

        // Stylisation dynamique du titre "Choisir les catégories"
        val highlighted = getString(R.string.category_word)
        val rawFormatted = getString(R.string.choose_categories, highlighted)
        val spannable = SpannableString(rawFormatted)
        val start = rawFormatted.indexOf(highlighted)
        val end = start + highlighted.length
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#C8007D")),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ui.title.text = spannable

        // Initialisation de Realm + chargement des séries de jeux depuis la base locale
        realm = Realm.getDefaultInstance()
        val storedSeries = realm.where(GameSeriesRealm::class.java).findAll()
        if (storedSeries.isEmpty()) {
            Toast.makeText(this, "Aucune donnée disponible", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        allGameseries.addAll(storedSeries.map { AmiiboGame(key = it.name, name = it.name) })

        // Création de l’adaptateur pour la ListView afin de lier les données des séries de jeux
        // à l’interface utilisateur
        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_multiple_choice,
            allGameseries.map { it.name }
        )
        ui.listViewCategories.adapter = adapter
        ui.listViewCategories.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        // Sélection aléatoire de 4 catégories par défaut
        val randomIndices = allGameseries.indices.shuffled().take(4)
        randomIndices.forEach { index -> ui.listViewCategories.setItemChecked(index, true) }

        ui.btnStartGame.setOnClickListener { handleStartGame() }
        ui.footerIconLeft.setOnClickListener { finish() }
    }

    /**
     * Gère le clic sur le bouton "Commencer" :
     * - Récupère les catégories sélectionnées
     * - Appelle l’API pour obtenir tous les amiibos
     * - Filtre les amiibos selon les catégories
     * - Stocke les résultats localement dans Realm
     * - Lance l'activité de jeu
     */
    private fun handleStartGame() {
        // Vérification de quelles catégories sont sélectionnées
        val checked = ui.listViewCategories.checkedItemPositions
        selectedGameseries.clear()

        // Vérification que l'utilisateur a sélectionné au moins 4 catégories
        for (i in 0 until adapter.count) {
            if (checked.get(i)) {
                selectedGameseries.add(allGameseries[i])
            }
        }

        // Si moins de 4 catégories sélectionnées, affiche un message d'erreur
        if (selectedGameseries.size < 4) {
            Toast.makeText(this, "Sélectionne au moins 4 catégories !", Toast.LENGTH_SHORT).show()
            return
        }

        //? Feedback utilisateur pendant le chargement
        ui.loaderLottie.visibility = View.VISIBLE
        ui.container.alpha = 0.5f
        ui.btnStartGame.isEnabled = false
        ui.listViewCategories.isEnabled = false
        ui.footerIconLeft.isEnabled = false

        //* Appel de l'API pour récupérer tous les amiibos nécessaires
        CoroutineScope(Dispatchers.IO).launch {
            val response = ApiClient.apiService.getAllAmiibos()

            if (!response.isSuccessful || response.body() == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Erreur API", Toast.LENGTH_SHORT).show()
                    resetUI()
                }
                return@launch
            }

            val allApiAmiibos = response.body()?.amiibo ?: emptyList()
            val selectedSeriesNames = selectedGameseries.map { it.name }

            // Filtrage des amiibos selon les séries sélectionnées
            val filteredAmiibos = allApiAmiibos.filter { it.gameSeries in selectedSeriesNames }

            Log.d("MainActivity", "→ ${filteredAmiibos.size} amiibos filtrés sur ${allApiAmiibos.size} totaux")

            //! Vérifie qu'il y a suffisamment d'amiibos pour le nombre de questions
            //! Nécessaire car le mode de jeu "Facile", "Moyen" ou "Difficile" peut changer le nombre de questions
            //! Cela impose d'avoir assez de questions pour le mode de jeu sélectionné
            val minQuestions = UserSettingsManager.getQuestionCount()
            if (filteredAmiibos.size < minQuestions) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Pas assez d'amiibos (${filteredAmiibos.size}) pour $minQuestions questions !",
                        Toast.LENGTH_LONG
                    ).show()
                    resetUI()
                }
                return@launch
            }

            //* Sauvegarde des amiibos filtrés dans Realm
            Realm.getDefaultInstance().use { localRealm ->
                localRealm.executeTransaction { transactionRealm ->
                    transactionRealm.delete(AmiiboRealm::class.java)

                    filteredAmiibos.forEach { apiAmiibo ->
                        val id = "${apiAmiibo.name}_${apiAmiibo.gameSeries}"
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

            // Lance GameActivity une fois les données stockées
            withContext(Dispatchers.Main) {
                resetUI()
                startActivity(Intent(this@MainActivity, GameActivity::class.java))
            }
        }
    }

    /**
     * Réactive les éléments de l’UI après chargement ou erreur.
     */
    private fun resetUI() {
        ui.loaderLottie.visibility = View.GONE
        ui.container.alpha = 1f
        ui.btnStartGame.isEnabled = true
        ui.listViewCategories.isEnabled = true
        ui.footerIconLeft.isEnabled = true
    }

    /**
     * Création du menu d’options dans la toolbar.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_getamiibos, menu)
        return true
    }

    /**
     * Gère les actions sur les éléments du menu (tout sélectionner / valider).
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_select_all -> {
                toggleSelectAll()
                // Mise à jour de l’icône selon l’état
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

    /**
     * Active/désactive la sélection de toutes les catégories.
     */
    private fun toggleSelectAll() {
        allSelected = !allSelected
        for (i in 0 until adapter.count) {
            ui.listViewCategories.setItemChecked(i, allSelected)
        }
        adapter.notifyDataSetChanged()
    }

    /**
     * Libération des ressources Realm à la fermeture de l’activité.
     */
    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}
