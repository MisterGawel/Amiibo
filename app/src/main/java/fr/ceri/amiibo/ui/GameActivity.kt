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
import fr.ceri.amiibo.data.realm.UserSettingsManager
import fr.ceri.amiibo.databinding.ActivityGameBinding
import fr.ceri.amiibo.utils.MusicManager
import fr.ceri.amiibo.utils.MusicReceiver
import fr.ceri.amiibo.utils.OnSwipeTouchListener
import io.realm.Realm
import kotlinx.coroutines.*

class GameActivity : AppCompatActivity() {

    private lateinit var ui: ActivityGameBinding
    private var score = 0
    private val askedAmiibos = mutableSetOf<String>()
    private var correctAnswerIndex = 0
    private var currentQuestionType = QuestionType.NAME
    private var questionCount = 0
    private var maxQuestions: Int = 10
    private val musicReceiver = MusicReceiver()

    enum class QuestionType { NAME, SERIES }

    override fun onCreate(savedInstanceState: Bundle?) {

        //* Initialisation de l'UI
        super.onCreate(savedInstanceState)
        ui = ActivityGameBinding.inflate(layoutInflater)
        setContentView(ui.root)
        setSupportActionBar(ui.toolbar)
        supportActionBar?.title = ""
        ui.root.setOnTouchListener(OnSwipeTouchListener(this, this))

        //* Initialisation de l'activité GameActivity
        setupListeners()
        updateScore()
        launchGame()

        //* Initialisation de la musique et des flags
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(musicReceiver, filter)
    }

    //* Implémentation d'un message d'avertissement en cas de retour en arrière lors du jeu
    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.dialog_exit_title)
            .setMessage(R.string.dialog_exit_message)
            .setPositiveButton(R.string.dialog_yes) { _, _ -> finish() }
            .setNegativeButton(R.string.dialog_no, null)
            .show()
    }

    //* Lancement de la partie à chaque création de l'activité
    private fun launchGame() {
        CoroutineScope(Dispatchers.Main).launch {

            //* Initialisation des paramètres de jeu
            maxQuestions = UserSettingsManager.getQuestionCount()
            ui.progressBarQuiz.max = maxQuestions
            ui.progressBarQuiz.progress = 0
            loadNextQuestion()

            //* Initialisation de la musique en récupérant l'état de la musique depuis Realm
            if (UserSettingsManager.isMusicEnabled()) {
                MusicManager.startMusic(this@GameActivity)
                ui.footerIconRight.setImageResource(R.drawable.musicon)
            } else {
                ui.footerIconRight.setImageResource(R.drawable.musicoff)
            }
        }
    }

    //* Configuration des boutons
    private fun setupListeners() {

        ui.btnAnswer1.setOnClickListener { checkAnswer(0) }
        ui.btnAnswer2.setOnClickListener { checkAnswer(1) }
        ui.btnAnswer3.setOnClickListener { checkAnswer(2) }

        ui.footerIconLeft.setOnClickListener { onBackPressed() }
        ui.footerIconRight.setOnClickListener { toggleMusic() }
    }

    //* Gestion de la musique en mettant à jour dans la base de données si la musique est activée ou non
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

    //* Gestion des réponses
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

    //* Mise à jour du score dans le menu "menu_game"
    private fun updateScore() {

        //! Besoin de mettre à jour le menu en le forcant à se rafraichir
        invalidateOptionsMenu()
    }

    private fun loadNextQuestion(forcedType: QuestionType? = null) {
        if (questionCount >= maxQuestions) {
            endGame()
            return
        }

        // 1. Essayer de récupérer une question du type demandé ou aléatoire
        val question = (application as AmiiboApplication).getRandomAmiiboQuestion(askedAmiibos, forcedType)

        // 2. Si pas de question pour ce type → essayer l'autre type
        if (question == null) {
            val fallbackType = when (forcedType) {
                QuestionType.NAME -> QuestionType.SERIES
                QuestionType.SERIES -> QuestionType.NAME
                else -> null
            }

            val fallbackQuestion = if (fallbackType != null)
                (application as AmiiboApplication).getRandomAmiiboQuestion(askedAmiibos, fallbackType)
            else null

            if (fallbackQuestion == null) {
                // 3. Si aucune question n'est possible → fin du quiz
                Toast.makeText(this, R.string.toast_no_more_questions, Toast.LENGTH_SHORT).show()
                endGame()
                return
            } else {
                // 4. Charger la question de secours
                loadQuestion(fallbackQuestion)
                return
            }
        }

        // 5. Si on a trouvé une question directement → la charger
        loadQuestion(question)
    }

    private fun loadQuestion(question: AmiiboQuestion) {
        val realm = Realm.getDefaultInstance()
        val all = realm.where(AmiiboRealm::class.java).findAll()
        val used = all.find { it.image == question.imageUrl }?.id
        if (used != null) askedAmiibos.add(used)

        questionCount++
        updateToolbarTitle()
        ObjectAnimator.ofInt(ui.progressBarQuiz, "progress", ui.progressBarQuiz.progress, questionCount)
            .setDuration(300)
            .start()

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

    //* Fin de la partie
    private fun endGame() {
        val intent = Intent(this, GameOverActivity::class.java)
        intent.putExtra("score", score)
        startActivity(intent)
        finish()
    }

    fun switchToNameQuestion() {
        score -= 1
        Toast.makeText(this, R.string.toast_skip_name_question, Toast.LENGTH_SHORT).show()
        updateScore()
        loadNextQuestion(QuestionType.NAME)
    }

    fun switchToGameSeriesQuestion() {
        score -= 1
        Toast.makeText(this, R.string.toast_skip_series_question, Toast.LENGTH_SHORT).show()
        updateScore()
        loadNextQuestion(QuestionType.SERIES)
    }

    //* Tous ce qui est gestion du menu ci dessous

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
                invalidateOptionsMenu()
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
        supportActionBar?.title = Html.fromHtml("<font color='#000000'>Question $questionCount/$maxQuestions</font>", Html.FROM_HTML_MODE_LEGACY)
    }


    //* Tous ce qui est gestion de l'activités ci dessous
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