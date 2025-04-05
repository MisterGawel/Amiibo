package fr.ceri.amiibo.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import fr.ceri.amiibo.databinding.ActivityGameBinding
import fr.ceri.amiibo.data.model.Amiibo
import fr.ceri.amiibo.shared.SharedAmiiboList
import kotlin.random.Random
import android.content.Intent
import android.animation.ObjectAnimator

class GameActivity : AppCompatActivity() {

    private lateinit var ui: ActivityGameBinding
    private var score = 0
    private var currentAmiibo: Amiibo? = null
    private val askedAmiibos = mutableSetOf<Amiibo>()
    private var correctAnswerIndex = 0
    private var currentQuestionType = QuestionType.NAME
    private var questionCount = 0
    private val maxQuestions = 10

    enum class QuestionType { NAME, SERIES }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityGameBinding.inflate(layoutInflater)
        setContentView(ui.root)

        setupListeners()
        updateScore()
        loadNextQuestion()
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
        val allAmiibos = SharedAmiiboList.list

        if (allAmiibos.isEmpty()) {
            Toast.makeText(this, "Aucun amiibo disponible.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Si tous les amiibos ont été posés, on recommence
        if (askedAmiibos.size == allAmiibos.size) {
            askedAmiibos.clear()
        }

        val amiibo = allAmiibos
            .filterNot { askedAmiibos.contains(it) }
            .randomOrNull()

        if (amiibo == null) {
            Toast.makeText(this, "Plus de questions disponibles", Toast.LENGTH_SHORT).show()
            return
        }

        currentAmiibo = amiibo
        askedAmiibos.add(amiibo)

        questionCount++
        ObjectAnimator.ofInt(ui.progressBarQuiz, "progress", ui.progressBarQuiz.progress, questionCount)
            .setDuration(300)
            .start()
        if (questionCount > maxQuestions) {
            endGame()
            return
        }

        // Charger image
        Glide.with(this)
            .load(amiibo.image)
            .into(ui.imgAmiibo)

        // Tirer le type de question aléatoirement
        currentQuestionType = if (Random.nextBoolean()) QuestionType.NAME else QuestionType.SERIES

        val questionText = when (currentQuestionType) {
            QuestionType.NAME -> "Quel est le nom de cet amiibo ?"
            QuestionType.SERIES -> "De quelle license vient cet amiibo ?"
        }

        ui.tvQuestion.text = questionText

        // Générer les réponses
        val choices = generateChoices(amiibo, allAmiibos, currentQuestionType)
        correctAnswerIndex = choices.indexOf(
            when (currentQuestionType) {
                QuestionType.NAME -> amiibo.name
                QuestionType.SERIES -> amiibo.gameSeries
            }
        )

        // Remplir les boutons
        ui.btnAnswer1.text = choices[0]
        ui.btnAnswer2.text = choices[1]
        ui.btnAnswer3.text = choices[2]
    }

    private fun generateChoices(
        correct: Amiibo,
        all: List<Amiibo>,
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

        val choices = mutableListOf<String>()
        choices.add(correctValue)
        choices.addAll(allValues.take(2))

        return choices.shuffled()
    }

    private fun endGame() {
        val intent = Intent(this, GameOverActivity::class.java)
        intent.putExtra("score", score)
        startActivity(intent)
        finish()
    }

}
