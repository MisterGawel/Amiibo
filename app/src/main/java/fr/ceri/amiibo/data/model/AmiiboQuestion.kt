package fr.ceri.amiibo.data.model

import fr.ceri.amiibo.ui.GameActivity

data class AmiiboQuestion(
    val imageUrl: String,
    val questionType: GameActivity.QuestionType,
    val choices: List<String>,
    val correctAnswerIndex: Int
)