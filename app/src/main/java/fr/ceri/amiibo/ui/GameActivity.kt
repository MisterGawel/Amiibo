package fr.ceri.amiibo.ui

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import fr.ceri.amiibo.data.realm.AmiiboRealm
import fr.ceri.amiibo.data.realm.UserSettingsManager
import fr.ceri.amiibo.databinding.ActivityGameBinding
import fr.ceri.amiibo.utils.OnSwipeTouchListener
import io.realm.Realm
import kotlinx.coroutines.*
import kotlin.random.Random

class GameActivity : AppCompatActivity() {

    private lateinit var ui: ActivityGameBinding
    private var score = 0
    private var currentAmiibo: AmiiboRealm? = null
    private val askedAmiibos = mutableSetOf<String>()
    private var correctAnswerIndex = 0
    private var currentQuestionType = QuestionType.NAME
    private var questionCount = 0
    private var maxQuestions: Int = 10

    enum class QuestionType { NAME, SERIES }

    override fun onCreate(savedInstanceState: Bundle?) {

        //* Initialisation de l'UI
        super.onCreate(savedInstanceState)
        ui = ActivityGameBinding.inflate(layoutInflater)
        setContentView(ui.root)
        ui.root.setOnTouchListener(OnSwipeTouchListener(this, this))

        setupListeners()
        updateScore()

        //* Initialisation des questions
        CoroutineScope(Dispatchers.Main).launch {
            maxQuestions = UserSettingsManager.getQuestionCount()
            ui.progressBarQuiz.max = maxQuestions
            ui.progressBarQuiz.progress = 0
            loadNextQuestion()
        }
    }

    private fun setupListeners() {
        ui.btnAnswer1.setOnClickListener { checkAnswer(0) }
        ui.btnAnswer2.setOnClickListener { checkAnswer(1) }
        ui.btnAnswer3.setOnClickListener { checkAnswer(2) }
        ui.footerIconLeft.setOnClickListener { finish() }
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
        ui.tvScore.text = score.toString()
    }

    private fun loadNextQuestion() {
        val realm = Realm.getDefaultInstance()
        val allAmiibos = realm.where(AmiiboRealm::class.java).findAll()

        if (allAmiibos.isEmpty()) {
            Toast.makeText(this, "Aucun amiibo disponible.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (askedAmiibos.size == allAmiibos.size) {
            askedAmiibos.clear()
        }

        val available = allAmiibos.filterNot { askedAmiibos.contains(it.id) }
        val amiibo = available.randomOrNull()

        if (amiibo == null) {
            Toast.makeText(this, "Plus de questions disponibles.", Toast.LENGTH_SHORT).show()
            return
        }

        currentAmiibo = amiibo
        askedAmiibos.add(amiibo.id)

        if (questionCount >= maxQuestions) {
            endGame()
            return
        }

        questionCount++
        ObjectAnimator.ofInt(ui.progressBarQuiz, "progress", ui.progressBarQuiz.progress, questionCount)
            .setDuration(300)
            .start()

        Glide.with(this).load(amiibo.image).into(ui.imgAmiibo)

        currentQuestionType = if (Random.nextBoolean()) QuestionType.NAME else QuestionType.SERIES

        val questionHtml = when (currentQuestionType) {
            QuestionType.NAME -> "<font color='#000000'>Quel est le nom de </font><font color='#C8007D'>cet amiibo</font>"
            QuestionType.SERIES -> "<font color='#000000'>De quelle licence vient </font><font color='#C8007D'>cet amiibo</font>"
        }

        ui.tvQuestion.text = Html.fromHtml(questionHtml, Html.FROM_HTML_MODE_LEGACY)

        val choices = generateChoices(amiibo, allAmiibos, currentQuestionType)
        correctAnswerIndex = choices.indexOf(
            when (currentQuestionType) {
                QuestionType.NAME -> amiibo.name
                QuestionType.SERIES -> amiibo.gameSeries
            }
        )

        ui.btnAnswer1.text = choices[0]
        ui.btnAnswer2.text = choices[1]
        ui.btnAnswer3.text = choices[2]
    }

    private fun generateChoices(
        correct: AmiiboRealm,
        all: List<AmiiboRealm>,
        type: QuestionType
    ): List<String> {
        val correctValue = when (type) {
            QuestionType.NAME -> correct.name
            QuestionType.SERIES -> correct.gameSeries
        }

        val allValues = all.map {
            when (type) {
                QuestionType.NAME -> it.name
                QuestionType.SERIES -> it.gameSeries
            }
        }.distinct().filterNot { it == correctValue }.shuffled()

        val choices = mutableListOf(correctValue)
        choices.addAll(allValues.take(2))

        return choices.shuffled()
    }

    private fun endGame() {
        val intent = Intent(this, GameOverActivity::class.java)
        intent.putExtra("score", score)
        startActivity(intent)
        finish()
    }

    //* Gestion des gestes (swipe à gauche/droite)
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

}