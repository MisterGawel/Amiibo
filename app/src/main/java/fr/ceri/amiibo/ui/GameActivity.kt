package fr.ceri.amiibo.ui

import android.animation.ObjectAnimator
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
    private var currentAmiibo: AmiiboRealm? = null
    private val askedAmiibos = mutableSetOf<String>()
    private var correctAnswerIndex = 0
    private var currentQuestionType = QuestionType.NAME
    private var questionCount = 0
    private var maxQuestions: Int = 10
    private val musicReceiver = MusicReceiver()

    enum class QuestionType { NAME, SERIES }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityGameBinding.inflate(layoutInflater)
        setContentView(ui.root)
        setSupportActionBar(ui.toolbar)
        supportActionBar?.title = ""
        ui.root.setOnTouchListener(OnSwipeTouchListener(this, this))

        setupListeners()
        updateScore()

        CoroutineScope(Dispatchers.Main).launch {
            maxQuestions = UserSettingsManager.getQuestionCount()
            ui.progressBarQuiz.max = maxQuestions
            ui.progressBarQuiz.progress = 0
            loadNextQuestion()
        }

        CoroutineScope(Dispatchers.Main).launch {
            if (UserSettingsManager.isMusicEnabled()) {
                MusicManager.startMusic(this@GameActivity)
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(musicReceiver, filter)
    }

    private fun setupListeners() {
        ui.btnAnswer1.setOnClickListener { checkAnswer(0) }
        ui.btnAnswer2.setOnClickListener { checkAnswer(1) }
        ui.btnAnswer3.setOnClickListener { checkAnswer(2) }
        ui.footerIconLeft.setOnClickListener { goMain() }
        ui.footerIconRight.setOnClickListener { toggleMusic() }
    }

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

    private fun goMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun checkAnswer(index: Int) {
        if (index == correctAnswerIndex) {
            score += 2
            Toast.makeText(this, "Bonne réponse ! +2", Toast.LENGTH_SHORT).show()
        } else {
            score -= 2
            Toast.makeText(this, "Mauvaise réponse ! -2", Toast.LENGTH_SHORT).show()
        }
        updateScore()
        loadNextQuestion()
    }

    private fun updateScore() {
        invalidateOptionsMenu()
    }

    private fun loadNextQuestion() {
        if (questionCount >= maxQuestions) {
            endGame()
            return
        }

        val question = (application as AmiiboApplication).getRandomAmiiboQuestion(askedAmiibos)
        if (question == null) {
            Toast.makeText(this, "Plus de questions disponibles", Toast.LENGTH_SHORT).show()
            return
        }

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

        val html = when (question.questionType) {
            QuestionType.NAME -> "<font color='#000000'>Quel est le nom de </font><font color='#C8007D'>cet amiibo</font>"
            QuestionType.SERIES -> "<font color='#000000'>De quelle licence vient </font><font color='#C8007D'>cet amiibo</font>"
        }

        ui.tvQuestion.text = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        ui.btnAnswer1.text = question.choices[0]
        ui.btnAnswer2.text = question.choices[1]
        ui.btnAnswer3.text = question.choices[2]
    }

    private fun endGame() {
        val intent = Intent(this, GameOverActivity::class.java)
        intent.putExtra("score", score)
        startActivity(intent)
        finish()
    }

    fun switchToNameQuestion() {
        score -= 1
        Toast.makeText(this, "Question passée (nom) : -1", Toast.LENGTH_SHORT).show()
        currentQuestionType = QuestionType.NAME
        updateScore()
        loadNextQuestion()
    }

    fun switchToGameSeriesQuestion() {
        score -= 1
        Toast.makeText(this, "Question passée (licence) : -1", Toast.LENGTH_SHORT).show()
        currentQuestionType = QuestionType.SERIES
        updateScore()
        loadNextQuestion()
    }

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

        // Score coloré (ex: Score: 6 en vert)
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
        supportActionBar?.title = "Question $questionCount/$maxQuestions"
    }

    override fun onPause() {
        super.onPause()
        MusicManager.pauseMusic()
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.Main).launch {
            if (UserSettingsManager.isMusicEnabled()) {
                MusicManager.resumeMusic()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(musicReceiver)
        MusicManager.stopMusic()
    }
}