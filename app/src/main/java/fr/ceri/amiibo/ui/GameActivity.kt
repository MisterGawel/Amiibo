package fr.ceri.amiibo.ui

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import fr.ceri.amiibo.AmiiboApplication
import fr.ceri.amiibo.R
import fr.ceri.amiibo.data.model.AmiiboQuestion
import fr.ceri.amiibo.data.realm.AmiiboRealm
import fr.ceri.amiibo.databinding.ActivityGameBinding
import fr.ceri.amiibo.utils.*
import io.realm.Realm
import kotlinx.coroutines.*

/**
 * Activité principale du jeu : gère l'affichage des questions, le score,
 * les interactions utilisateur (clics, swipe), et la musique.
 */
class GameActivity : AppCompatActivity() {

    private lateinit var ui: ActivityGameBinding
    private var score = 0
    private val askedAmiibos = mutableSetOf<String>()
    private var correctAnswerIndex = 0
    private var currentQuestionType = QuestionType.NAME
    private var questionCount = 0
    private var maxQuestions: Int = 10
    private val musicReceiver = MusicReceiver()

    // Types de questions  : nom ou série
    enum class QuestionType { NAME, SERIES }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityGameBinding.inflate(layoutInflater)
        setContentView(ui.root)
        setSupportActionBar(ui.toolbar)
        supportActionBar?.title = ""

        // Ajout du détecteur de swipe (gauche/droite)
        ui.root.setOnTouchListener(OnSwipeTouchListener(this, this))

        setupListeners()
        updateScore()
        launchGame()

        // Enregistre le MusicReceiver pour gérer l'écran éteint / allumé
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(musicReceiver, filter)
    }

    /**
     * Confirmation avant de quitter la partie via le bouton retour.
     */
    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_exit_title)
            .setMessage(R.string.dialog_exit_message)
            .setPositiveButton(R.string.dialog_yes) { _, _ -> finish() }
            .setNegativeButton(R.string.dialog_no, null)
            .show()
    }

    /**
     * Initialise la partie : récupère les paramètres utilisateur
     * et charge la première question.
     */
    private fun launchGame() {
        CoroutineScope(Dispatchers.Main).launch {
            maxQuestions = UserSettingsManager.getQuestionCount()
            ui.progressBarQuiz.max = maxQuestions
            ui.progressBarQuiz.progress = 0
            loadNextQuestion()

            // Gère la musique selon les préférences de l'utilisateur
            if (UserSettingsManager.isMusicEnabled()) {
                MusicManager.startMusic(this@GameActivity)
                ui.footerIconRight.setImageResource(R.drawable.musicon)
            } else {
                ui.footerIconRight.setImageResource(R.drawable.musicoff)
            }
        }
    }

    /**
     * Associe les boutons de réponses et les icônes de bas de page à leur comportement.
     */
    private fun setupListeners() {
        ui.btnAnswer1.setOnClickListener { checkAnswer(0) }
        ui.btnAnswer2.setOnClickListener { checkAnswer(1) }
        ui.btnAnswer3.setOnClickListener { checkAnswer(2) }
        ui.footerIconLeft.setOnClickListener { onBackPressed() }
        ui.footerIconRight.setOnClickListener { toggleMusic() }
    }

    /**
     * Active ou désactive la musique et met à jour les préférences.
     */
    private fun toggleMusic() {
        CoroutineScope(Dispatchers.Main).launch {
            val isMusicEnabled = UserSettingsManager.isMusicEnabled()
            if (isMusicEnabled) {
                MusicManager.stopMusic()
                UserSettingsManager.setMusicEnabled(false)
                ui.footerIconRight.setImageResource(R.drawable.musicoff)
            } else {
                MusicManager.startMusic(this@GameActivity)
                UserSettingsManager.setMusicEnabled(true)
                ui.footerIconRight.setImageResource(R.drawable.musicon)
            }
        }
    }

    /**
     * Vérifie la réponse choisie par l’utilisateur.
     */
    private fun checkAnswer(index: Int) {
        if (index == correctAnswerIndex) {
            score += 2
            Toast.makeText(this, R.string.toast_correct_answer, Toast.LENGTH_SHORT).show()
        } else {
            score -= 2
            Toast.makeText(this, R.string.toast_wrong_answer, Toast.LENGTH_SHORT).show()
        }

        updateScore()
        loadNextQuestion()
    }

    /**
     * Rafraîchit l'affichage du score.
     */
    private fun updateScore() {
        invalidateOptionsMenu() // Force la mise à jour du menu
    }

    /**
     * Charge une nouvelle question. Si aucune n’est disponible, termine la partie.
     */
    private fun loadNextQuestion(forcedType: QuestionType? = null) {
        if (questionCount >= maxQuestions) {
            endGame()
            return
        }

        val question = (application as AmiiboApplication).getRandomAmiiboQuestion(askedAmiibos, forcedType)

        if (question == null) {
            // on essaie l'autre type
            val fallbackType = when (forcedType) {
                QuestionType.NAME -> QuestionType.SERIES
                QuestionType.SERIES -> QuestionType.NAME
                else -> null
            }

            val fallbackQuestion = fallbackType?.let {
                (application as AmiiboApplication).getRandomAmiiboQuestion(askedAmiibos, it)
            }

            if (fallbackQuestion == null) {
                Toast.makeText(this, R.string.toast_no_more_questions, Toast.LENGTH_SHORT).show()
                endGame()
            } else {
                loadQuestion(fallbackQuestion)
            }
        } else {
            loadQuestion(question)
        }
    }

    /**
     * Affiche la question et met à jour les composants visuels.
     */
    private fun loadQuestion(question: AmiiboQuestion) {

        // On récupère l'instance de la base de données Realm
        val realm = Realm.getDefaultInstance()
        val all = realm.where(AmiiboRealm::class.java).findAll()

        // On ajoute l'amiibo à la liste des déjà vus
        val used = all.find { it.image == question.imageUrl }?.id
        if (used != null) askedAmiibos.add(used)

        // On met à jour le compteur de questions et le menu
        questionCount++
        updateToolbarTitle()

        // Animation de progression
        ObjectAnimator.ofInt(ui.progressBarQuiz, "progress", ui.progressBarQuiz.progress, questionCount)
            .setDuration(300)
            .start()

        // Affichage de la question
        Glide.with(this).load(question.imageUrl).into(ui.imgAmiibo)
        currentQuestionType = question.questionType
        correctAnswerIndex = question.correctAnswerIndex

        val amiiboPlaceholder = getString(R.string.cet_amiibo)

        val text = when (question.questionType) {
            QuestionType.NAME -> getString(R.string.question_name_html, amiiboPlaceholder)
            QuestionType.SERIES -> getString(R.string.question_series_html, amiiboPlaceholder)
        }
        ui.tvQuestion.text = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)

        val choices = question.choices
        if (choices.size >= 3) {
            ui.btnAnswer1.text = choices[0]
            ui.btnAnswer2.text = choices[1]
            ui.btnAnswer3.text = choices[2]
        } else {
            Toast.makeText(this, "Plus assez de réponses (FIN DU JEU)", Toast.LENGTH_SHORT).show()
            endGame()
        }
    }

    /**
     * Termine la partie et redirige vers GameOverActivity.
     */
    private fun endGame() {
        val intent = Intent(this, GameOverActivity::class.java)
        intent.putExtra("score", score)
        startActivity(intent)
        finish()
    }

    /**
     * Permet de sauter à une question de type "nom" via swipe gauche.
     */
    fun switchToNameQuestion() {
        score -= 1
        Toast.makeText(this, R.string.toast_skip_name_question, Toast.LENGTH_SHORT).show()
        updateScore()
        loadNextQuestion(QuestionType.NAME)
    }

    /**
     * Permet de sauter à une question de type "série" via swipe droit.
     */
    fun switchToGameSeriesQuestion() {
        score -= 1
        Toast.makeText(this, R.string.toast_skip_series_question, Toast.LENGTH_SHORT).show()
        updateScore()
        loadNextQuestion(QuestionType.SERIES)
    }

    /**
     * Menu de la barre d'action avec affichage du score.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_game, menu)
        updateScoreMenuItem(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_reset -> {
                score = 0
                updateScore()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateScoreMenuItem(menu: Menu?) {
        val scoreItem = menu?.findItem(R.id.item_score)
        val scoreText = "Score: $score"
        val spannable = android.text.SpannableString(scoreText)

        val color = when {
            score > 0 -> android.graphics.Color.GREEN
            score == 0 -> android.graphics.Color.BLACK
            else -> android.graphics.Color.RED
        }

        spannable.setSpan(
            android.text.style.ForegroundColorSpan(color),
            0, scoreText.length,
            android.text.Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )

        scoreItem?.title = spannable
    }

    private fun updateToolbarTitle() {
        supportActionBar?.title =
            Html.fromHtml("<font color='#000000'>Question $questionCount/$maxQuestions</font>", Html.FROM_HTML_MODE_LEGACY)
    }

    //* Cycle de vie : gestion de la musique
    override fun onPause() {
        super.onPause()
        MusicManager.pauseMusic()
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.Main).launch {
            if (UserSettingsManager.isMusicEnabled()) {
                MusicManager.startMusic(this@GameActivity)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MusicManager.stopMusic()
        unregisterReceiver(musicReceiver)
        Realm.getDefaultInstance().close()
    }
}
